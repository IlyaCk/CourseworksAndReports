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
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class BackgroundService {
    private final GoogleDriveService googleDriveService;
    private final WorkRepository workRepository;
    private final DisciplineRepository disciplineRepository;
    private final DisciplineUpdateNotifier notifier;

    private MatchLevel castMatchLevel(StrDist.MatchLevel matchLevel) {
        switch (matchLevel) {
            case HIGH -> { return MatchLevel.HIGH; }
            case MEDIUM -> { return MatchLevel.MEDIUM; }
            case LOW -> { return MatchLevel.LOW; }
            case NOT_MATCHED -> { return MatchLevel.NOT_MATCHED; }
        }
        System.err.printf("Unknown match level: %s\n", matchLevel);
        return MatchLevel.NOT_MATCHED;
    }

    @Async("asyncExecutor")
    public void verifyWorks(String accessToken, Discipline discipline, Department department) throws GeneralSecurityException, IOException {
        Set<Work> works = discipline.getWorks();

        String programFolderId = googleDriveService.createFolderIfNotExists(accessToken,"CourseworkManagement", null);
        String disciplineFolderId = googleDriveService.createFolderIfNotExists(accessToken, discipline.getName(), programFolderId);

        for (Work work : works) {
            String firstPage = PDFTools.extractFirstPageText(googleDriveService.getFileContent(accessToken, work.getClassroomLink()));
            if (work.getStudent() != null) {
                List<String> fullNameVariants = PDFTools.getVariants(work.getStudent().getName());
                List<String> searchVariants = new ArrayList<>();
                switch (discipline.getNameFormat()){
                    case SURNAME_NAME -> searchVariants.add(fullNameVariants.getFirst());
                    case SURNAME_I -> searchVariants.add(fullNameVariants.get(1));
                    case SURNAME_IB -> searchVariants.add(fullNameVariants.get(2));
                    case SURNAME_NAME_PATRONYMIC -> searchVariants.add(fullNameVariants.get(3));
                    default -> searchVariants.addAll(fullNameVariants);
                }

                double minDist = Integer.MAX_VALUE;
                for (String fullName : searchVariants) {
                    StrDist.DistResInfo distInfo = StrDist.getBestMatchWordRow(fullName, firstPage, true);
                    double thisDist = distInfo.dist / Math.sqrt(fullName.length());
                    System.out.println(fullName + " -> " + thisDist);
                    if (thisDist < minDist) {
                        minDist = thisDist;
                        work.setIsCorrectStudent(castMatchLevel(distInfo.matchLevel));
                        work.setStudentDifference(distInfo.diffAsHtml);
                    }
                }
            }
            if (work.getSupervisor() != null) {
                List<String> fullNameVariants = PDFTools.getVariants(work.getSupervisor().getName());
                double minDist = Integer.MAX_VALUE;
                for (String fullName : fullNameVariants) {
                    StrDist.DistResInfo distInfo = StrDist.getBestMatchWordRow(fullName, firstPage, true);
                    double thisDist = distInfo.dist / Math.sqrt(fullName.length());
                    System.out.println(fullName + " -> " + thisDist);
                    if (thisDist < minDist) {
                        minDist = thisDist;
                        work.setIsCorrectSupervisor(castMatchLevel(distInfo.matchLevel));
                        work.setSupervisorDifference(distInfo.diffAsHtml);
                    }
                }
            }
            if (work.getTheme() != null) {
                StrDist.DistResInfo distInfo = StrDist.getBestMatchWordRow(work.getTheme(), firstPage, true);
                work.setIsCorrectTheme(castMatchLevel(distInfo.matchLevel));
                work.setThemeDifference(distInfo.diffAsHtml);
            }
            if (work.getStudentGroup() != null){
                work.setGroupDifference(StrDist.getBestMatchWord("групи " + work.getStudentGroup(), firstPage, true).diffAsHtml);
            }

            work.setMinistryDifference(StrDist.getBestMatchRow(department.getMinistry(), firstPage, true).diffAsHtml);

            work.setHEIDifference(StrDist.getBestMatchRow(department.getHEI(), firstPage, true).diffAsHtml);

            work.setDepartmentDifference(StrDist.getBestMatchRow(department.getName(), firstPage, true).diffAsHtml);

            work.setCityYearDifference(StrDist.getBestMatchWord(department.getCityYear() + " – " + discipline.getYear(), firstPage, true).diffAsHtml);

            String fullLink = googleDriveService.copyFile(
                    accessToken,
                    work.getClassroomLink(),
                    discipline.getName() + "_" + work.getStudent().getName() + "_ПОВНА.pdf",
                    disciplineFolderId
            );
            work.setFullTextLink(fullLink);

            /* byte[] shortVersion = PDFTools.removeAppendices(
                    googleDriveService.getFileContent(accessToken, work.getClassroomLink())
            );

            String shortLink = googleDriveService.uploadFile(
                    accessToken,
                    discipline.getName() + "_" + work.getStudent().getName() + "_БЕЗ_ДОДАТКІВ.pdf",
                    "application/pdf",
                    shortVersion,
                    disciplineFolderId
            );
            work.setShortTextLink(shortLink);*/

            workRepository.save(work);
        }
        discipline.setUpdating(false);
        disciplineRepository.save(discipline);
        notifier.notifyListeners(discipline.getId());
    }


}
