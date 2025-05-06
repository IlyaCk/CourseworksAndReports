package com.example.demo.service;

import com.example.demo.entity.Discipline;
import com.example.demo.entity.MatchLevel;
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

    @Async("asyncExecutor")
    public void verifyWorks(String accessToken, Discipline discipline) throws GeneralSecurityException, IOException {
        Set<Work> works = discipline.getWorks();
        for (Work work : works) {
            String firstPage = PDFTools.extractFirstPageText(googleDriveService.getFileContent(accessToken, work.getClassroomLink()));
            if (work.getStudent() != null) {
                List<String> fullNameVariants = PDFTools.getVariants(work.getStudent().getName());
                int minDist = Integer.MAX_VALUE;
                for (String fullName : fullNameVariants) {
                    StrDist.DistResInfo distInfo = getBestMatch(fullName, firstPage);
                    if (distInfo.dist < minDist) {
                        minDist = distInfo.dist;
                        work.setIsCorrectStudent(calculateMatchLevel(distInfo.dist));
                        work.setStudentDifference(distInfo.diffAsHtml);
                    }
                }
            }
            if (work.getSupervisor() != null) {
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
            work.setMinistryDifference(getBestMatch("Міністерство освіти і науки України", firstPage).diffAsHtml);
            work.setHEIDifference(getBestMatch("Черкаський національний університет імені Богдана Хмельницького", firstPage).diffAsHtml);
            work.setDepartmentDifference(getBestMatch("Кафедра програмного забезпечення автоматизованих систем", firstPage).diffAsHtml);
            work.setGroupDifference(getBestMatch("КС-21", firstPage).diffAsHtml);
            work.setCityYearDifference(getBestMatch("Черкаси – 2025", firstPage).diffAsHtml);
            workRepository.save(work);
        }
        discipline.setUpdating(false);
        disciplineRepository.save(discipline);
        notifier.notifyListeners(discipline.getId());
    }

    private StrDist.DistResInfo getBestMatch(String substr, String str) throws IOException {
        StrDist.DistResInfo distInfo = StrDist.calcStrDist(substr, str,
                EnumSet.of(StrDist.MatchOption.AS_SUBSTR,
                        StrDist.MatchOption.DO_RESTORE_PATH,
                        StrDist.MatchOption.DO_DISCOUNT_REPEAT_INSERT));
        StrDist.DistResInfo distInfoUpperCase = StrDist.calcStrDist(substr.toUpperCase(Locale.ROOT), str,
                EnumSet.of(StrDist.MatchOption.AS_SUBSTR,
                        StrDist.MatchOption.DO_RESTORE_PATH,
                        StrDist.MatchOption.DO_DISCOUNT_REPEAT_INSERT));
//        StrDist.DistResInfo distInfo = StrDist.calcStrDist(substr, str, true, false);
//        StrDist.DistResInfo distInfoUpperCase = StrDist.calcStrDist(substr.toUpperCase(Locale.ROOT), str, true, false);
        if (distInfo.dist <= distInfoUpperCase.dist) {
            return distInfo;
        } else {
            return distInfoUpperCase;
        }
    }

    private MatchLevel calculateMatchLevel(int dist) {
        if (dist < 10) return MatchLevel.HIGH;
        else if (dist < 30) return MatchLevel.MEDIUM;
        else if (dist < 100) return MatchLevel.LOW;
        else return MatchLevel.NOT_MATCHED;
    }
}
