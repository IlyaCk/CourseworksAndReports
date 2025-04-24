package com.example.demo.service;

import com.example.demo.entity.Discipline;
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
//            String firstPageNormalized = PDFTools.normalizeTitleText(firstPage);
            if (work.getStudent() != null) {
                work.setCorrectStudent(PDFTools.fuzzyMatchFullName(work.getStudent().getName(), firstPage));
            }
            if (work.getSupervisor() != null) {
                work.setCorrectSupervisor(PDFTools.fuzzyMatchFullName(work.getSupervisor().getName(), firstPage));
            }
            if (work.getTheme() != null){
//                work.setCorrectTheme(firstPage.toLowerCase().trim().contains(work.getTheme().toLowerCase().trim()));
                StrDist.DistResInfo distInfo = StrDist.calcStrDist(work.getTheme(), firstPage, true, false);
                StrDist.DistResInfo distInfoUpperCase = StrDist.calcStrDist(work.getTheme().toUpperCase(Locale.ROOT), firstPage, true, false);
//                StrDist.DistResInfo distInfoTwo = StrDist.calcStrDist(work.getTheme(), firstPage, true, true);
                System.out.println(distInfo.diffAsHtml);
                System.out.println("dist = " + distInfo.dist);
                System.out.println("<<" + work.getTheme() + ">>");
                System.out.println(distInfo.diffAsHtml);
                System.out.println("dist = " + distInfoUpperCase.dist);
                System.out.println("<<" + work.getTheme().toUpperCase(Locale.ROOT) + ">>");
                System.out.println(distInfoUpperCase.diffAsHtml);
                if (distInfoUpperCase.dist == 32) {
                    for (int i = 0; i < distInfoUpperCase.diffAsHtml.length(); i++) {
                        System.out.println(distInfoUpperCase.diffAsHtml.charAt(i) + "\t" + (int)(distInfoUpperCase.diffAsHtml.charAt(i)));
                    }
                }
//                System.out.println("distTwo = " + distInfoTwo.dist);
//                System.out.println(distInfoTwo.diffAsHtml);

//                work.setCorrectTheme(distInfo.dist < 16 || distInfoTwo.dist < 0); // TODO: replace boolean with multi-level estimate
                work.setCorrectTheme(distInfo.dist < 16 || distInfoUpperCase.dist < 16); // TODO: replace boolean with multi-level estimate
            }
            workRepository.save(work);
        }
        discipline.setUpdating(false);
        disciplineRepository.save(discipline);
        notifier.notifyListeners(discipline.getId());
    }
}
