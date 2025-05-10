package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.enums.FileNameTemplate;
import com.example.demo.entity.enums.MatchLevel;
import com.example.demo.entity.Work;
import com.example.demo.entity.enums.WorkState;
import com.example.demo.repository.DisciplineRepository;
import com.example.demo.repository.WorkRepository;
import com.example.demo.utils.PDFTools;
import com.example.demo.utils.StrDist;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BackgroundService {
    private final GoogleDriveService googleDriveService;
    private final WorkRepository workRepository;
    private final DisciplineRepository disciplineRepository;
    private final DisciplineUpdateNotifier notifier;

    @Async("asyncExecutor")
    public void verifyWorks(String accessToken, Discipline discipline, Department department) throws GeneralSecurityException, IOException {
        Set<Work> works = discipline.getWorks();

        String programFolderId = googleDriveService.createFolderIfNotExists(accessToken, "CourseworkManagement", null);
        String disciplineFolderId = googleDriveService.createFolderIfNotExists(accessToken, discipline.getName() + "-" + discipline.getYear(), programFolderId);
        discipline.setGoogleDriveFolderLink(
                "https://drive.google.com/drive/folders/" + disciplineFolderId
        );

        for (Work work : works) {

            if (work.getState() == WorkState.DEFAULT) {
                continue;
            }

            byte[] originalFileContent;
            try {
                originalFileContent = googleDriveService.getFileContent(accessToken, work.getClassroomLink()).readAllBytes();
            } catch (IOException e) {
                workRepository.save(work);
                continue;
            }
            List<String> relatedUserEmails = new ArrayList<>();
            String firstPage = PDFTools.extractFirstPageText(googleDriveService.getFileContent(accessToken, work.getClassroomLink()));
            if (work.getStudent() != null) {
                relatedUserEmails.add(work.getStudent().getEmail());
                List<String> fullNameVariants = PDFTools.getVariants(work.getStudent().getName());
                List<String> searchVariants = new ArrayList<>();
                if (fullNameVariants.size() == 4) {
                    switch (discipline.getNameFormat()) {
                        case SURNAME_NAME -> searchVariants.add(fullNameVariants.getFirst());
                        case SURNAME_I -> searchVariants.add(fullNameVariants.get(1));
                        case SURNAME_IB -> searchVariants.add(fullNameVariants.get(2));
                        case SURNAME_NAME_PATRONYMIC -> searchVariants.add(fullNameVariants.get(3));
                        default -> searchVariants.addAll(fullNameVariants);
                    }
                }

                int minDist = Integer.MAX_VALUE;
                for (String fullName : searchVariants) {
                    StrDist.DistResInfo distInfo = getBestMatch(fullName, firstPage);
                    if (distInfo.dist < minDist) {
                        minDist = distInfo.dist;
                        work.setIsCorrectStudent(calculateMatchLevel(distInfo.dist));
                        work.setStudentDifference(distInfo.diffAsHtml);
                    }
                }
            }
            if (work.getSupervisor() != null) {
                relatedUserEmails.add(work.getSupervisor().getEmail());
                List<String> fullNameVariants = PDFTools.getVariants(work.getSupervisor().getName());
                int minDist = Integer.MAX_VALUE;
                for (String fullName : fullNameVariants) {
                    StrDist.DistResInfo distInfo = getBestMatch(fullName, firstPage);
                    if (distInfo.dist < minDist) {
                        minDist = distInfo.dist;
                        work.setIsCorrectSupervisor(calculateMatchLevel(distInfo.dist));
                        work.setSupervisorDifference(distInfo.diffAsHtml);
                    }
                }
            }
            if (work.getTheme() != null) {
                StrDist.DistResInfo distInfo = getBestMatch(work.getTheme(), firstPage);
                work.setIsCorrectTheme(calculateMatchLevel(distInfo.dist));
                work.setThemeDifference(distInfo.diffAsHtml);
            }
            if (work.getStudentGroup() != null) {
                work.setGroupDifference(getBestMatch(work.getStudentGroup(), firstPage).diffAsHtml);
            }
            if (work.getReviewer() != null){
                relatedUserEmails.add(work.getReviewer().getEmail());
            }

            work.setMinistryDifference(getBestMatch(department.getMinistry(), firstPage).diffAsHtml);
            work.setHEIDifference(getBestMatch(department.getHEI(), firstPage).diffAsHtml);
            work.setDepartmentDifference(getBestMatch(department.getName(), firstPage).diffAsHtml);
            work.setCityYearDifference(getBestMatch(department.getCityYear(), firstPage).diffAsHtml);

            List<FileNameTemplate> enumList = Arrays.stream(discipline.getFileNameTemplate().split("_"))
                    .map(name -> Enum.valueOf(FileNameTemplate.class, name))
                    .toList();

            StringBuilder filename = new StringBuilder();
            for (FileNameTemplate myEnum : enumList) {
                switch (myEnum) {
                    case TYPE -> filename.append("{0}_");
                    case STUDENT ->
                            filename.append(PDFTools.getUserNameForFile(work.getStudent().getName())).append("_");
                    case DISCIPLINE -> filename.append(discipline.getName()).append("_");
                    case GROUP -> filename.append((work.getStudentGroup() != null ? work.getStudentGroup() + "_" : ""));
                }
            }
            if (filename.lastIndexOf("_") == filename.length() - 1) {
                filename.deleteCharAt(filename.length() - 1);
            }
            filename.append(".pdf");

            if (work.getState() == WorkState.UPDATE) {
                String fullTextFileId = googleDriveService.updateFileContent(
                        accessToken,
                        work.getFullTextLink(),
                        originalFileContent
                );
                work.setFullTextLink("https://drive.google.com/file/d/" + fullTextFileId + "/view");

                byte[] trimmedPdfContent = PDFTools.trimAppendicesAndGetContent(originalFileContent);
                if (trimmedPdfContent != null && trimmedPdfContent.length > 0) {
                    String trimmedTextFileId = googleDriveService.updateFileContent(
                            accessToken,
                            work.getShortTextLink(),
                            trimmedPdfContent
                    );
                    work.setShortTextLink("https://drive.google.com/file/d/" + trimmedTextFileId + "/view");
                }
            } else if (work.getState() != WorkState.ONLY_DATA_UPDATE) {
                String fullTextFileId = googleDriveService.copyFile(
                        accessToken,
                        work.getClassroomLink(),
                        MessageFormat.format(filename.toString(), "ПОВНА"),
                        disciplineFolderId
                );
                relatedUserEmails = department.getHeadUsers().stream()
                        .map(user -> user.getEmail())
                        .filter(email -> !email.equals(department.getResponsibleUser().getEmail()))
                        .collect(Collectors.toCollection(() -> new LinkedHashSet<>())).stream().toList();

//            googleDriveService.addViewerPermissionsToMultipleUsers(
//                    accessToken,
//                    fullTextFileId,
//                    relatedUserEmails
//            );
                work.setFullTextLink("https://drive.google.com/file/d/" + fullTextFileId + "/view");

                byte[] trimmedPdfContent = PDFTools.trimAppendicesAndGetContent(originalFileContent);
                if (trimmedPdfContent != null && trimmedPdfContent.length > 0) {
                    String trimmedTextFileId = googleDriveService.uploadFile(
                            accessToken,
                            MessageFormat.format(filename.toString(), "БЕЗ_ДОДАТКІВ"),
                            "application/pdf",
                            trimmedPdfContent,
                            disciplineFolderId
                    );
//                googleDriveService.addViewerPermissionsToMultipleUsers(
//                        accessToken,
//                        trimmedTextFileId,
//                        relatedUserEmails
//                );
                    work.setShortTextLink("https://drive.google.com/file/d/" + trimmedTextFileId + "/view");
                }
            }
            work.setState(WorkState.DEFAULT);
            workRepository.save(work);
        }
        discipline.setUpdating(false);
        discipline.setUpdateDate(LocalDateTime.now());
        disciplineRepository.save(discipline);
        notifier.notifyListeners(discipline.getId());
    }

    private StrDist.DistResInfo getBestMatch(String substr, String str) throws IOException {
        StrDist.DistResInfo distInfo = StrDist.calcStrDist(substr, str, true, false);
        StrDist.DistResInfo distInfoUpperCase = StrDist.calcStrDist(substr.toUpperCase(Locale.ROOT), str, true, false);
        if (distInfo.dist <= distInfoUpperCase.dist) {
            return distInfo;
        } else {
            return distInfoUpperCase;
        }
    }

    private MatchLevel calculateMatchLevel(int dist) {
        if (dist < 10) return MatchLevel.HIGH;
        else if (dist < 20) return MatchLevel.MEDIUM;
        else if (dist < 40) return MatchLevel.LOW;
        else return MatchLevel.NOT_MATCHED;
    }
}
