package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.enums.MatchLevel;
import com.example.demo.entity.Work;
import com.example.demo.repository.DisciplineRepository;
import com.example.demo.repository.WorkRepository;
import com.example.demo.utils.PDFTools;
import com.example.demo.utils.StrDist;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
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

        for (Work work : works) {

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
                switch (discipline.getNameFormat()) {
                    case SURNAME_NAME -> searchVariants.add(fullNameVariants.getFirst());
                    case SURNAME_I -> searchVariants.add(fullNameVariants.get(1));
                    case SURNAME_IB -> searchVariants.add(fullNameVariants.get(2));
                    case SURNAME_NAME_PATRONYMIC -> searchVariants.add(fullNameVariants.get(3));
                    default -> searchVariants.addAll(fullNameVariants);
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

            work.setMinistryDifference(getBestMatch(department.getMinistry(), firstPage).diffAsHtml);
            work.setHEIDifference(getBestMatch(department.getHEI(), firstPage).diffAsHtml);
            work.setDepartmentDifference(getBestMatch(department.getName(), firstPage).diffAsHtml);
            work.setCityYearDifference(getBestMatch(department.getCityYear(), firstPage).diffAsHtml);

            String filename = discipline.getName() + "_"
                    + (work.getStudentGroup() != null ? work.getStudentGroup() + "_" : "")
                    + PDFTools.getUserNameForFile(work.getStudent().getName());

            String fullTextFileId = googleDriveService.copyFile(
                    accessToken,
                    work.getClassroomLink(),
                    filename + "_ПОВНА.pdf",
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
                String trimmedFileName = discipline.getName() + "_" + PDFTools.getUserNameForFile(work.getStudent().getName()) + "_БЕЗ_ДОДАТКІВ.pdf";
                String trimmedTextFileId = googleDriveService.uploadFile(
                        accessToken,
                        trimmedFileName,
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

            workRepository.save(work);
        }
        discipline.setUpdating(false);
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
