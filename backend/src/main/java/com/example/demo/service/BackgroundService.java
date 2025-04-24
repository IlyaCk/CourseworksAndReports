package com.example.demo.service;

import com.example.demo.entity.Discipline;
import com.example.demo.entity.Work;
import com.example.demo.repository.DisciplineRepository;
import com.example.demo.repository.WorkRepository;
import com.example.demo.utils.PDFTools;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
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
            firstPage = PDFTools.normalizeTitleText(firstPage);
            if (work.getStudent() != null) {
                work.setCorrectStudent(PDFTools.fuzzyMatchFullName(work.getStudent().getName(), firstPage));
            }
            if (work.getSupervisor() != null) {
                work.setCorrectSupervisor(PDFTools.fuzzyMatchFullName(work.getSupervisor().getName(), firstPage));
            }
            if (work.getTheme() != null){
                work.setCorrectTheme(firstPage.toLowerCase().trim().contains(work.getTheme().toLowerCase().trim()));
                work.setThemeDifference("<p>\n" +
                        "<span class=\"goo\">Міністерство освіти </span><span class=\"baaa\">і</span><span class=\"skipped\">та</span><span class=\"goo\"> науки України \n" +
                        "Черкаський національний університет ім</span><span class=\"baaa\">ені</span><span class=\"skipped\">.</span><span class=\"goo\"> Б</span><span class=\"baaa\">огдана </span><span class=\"skipped\">.</span><span class=\"goo\">Хмельницького \n" +
                        "Факультет обчислювальної техніки, ін</span><span class=\"baaa\">телект</span><span class=\"skipped\">форма</span><span class=\"goo\">уа</span><span class=\"baaa\">ль</span><span class=\"skipped\">цій</span><span class=\"goo\">них </span><span class=\"baaa\">та</span><span class=\"skipped\">і</span><span class=\"goo\"> управляючих систем \n" +
                        "Кафедра </span><span class=\"baaa\">прогр</span><span class=\"skipped\">матем</span><span class=\"goo\">а</span><span class=\"baaa\">м</span><span class=\"skipped\">тич</span><span class=\"goo\">ного забезпечення а</span><span class=\"baaa\">в</span><span class=\"goo\">томатизованих с</span><span class=\"baaa\">и</span><span class=\"skipped\">е</span><span class=\"goo\">стем</span><span class=\"skipped\">\n" +
                        "До захисту допускаю\n" +
                        "Завідувачка кафедри ПЗАС Супруненко О. О.</span><span class=\"goo\">\n" +
                        "К</span><span class=\"baaa\">урсова</span><span class=\"skipped\">УРСОВА</span><span class=\"goo\"> </span><span class=\"baaa\">робота </span><span class=\"skipped\">РОБОТА\n" +
                        "</span><span class=\"goo\">з </span><span class=\"baaa\">о</span><span class=\"skipped\">дисципліни «О</span><span class=\"goo\">б’єкт</span><span class=\"skipped\">ив</span><span class=\"goo\">н</span><span class=\"baaa\">о-</span><span class=\"skipped\">е </span><span class=\"goo\">оріє</span><span class=\"skipped\">т</span><span class=\"goo\">нт</span><span class=\"skipped\">н</span><span class=\"goo\">ов</span><span class=\"baaa\">а</span><span class=\"goo\">н</span><span class=\"baaa\">ого</span><span class=\"skipped\">е</span><span class=\"goo\"> прог</span><span class=\"baaa\">ра</span><span class=\"skipped\">у</span><span class=\"goo\">мування</span><span class=\"skipped\">»</span><span class=\"goo\">\n" +
                        "</span><span class=\"skipped\">на тему: </span><span class=\"goo\">Програ</span><span class=\"baaa\">м</span><span class=\"goo\">ний дода</span><span class=\"baaa\">т</span><span class=\"skipped\">нк</span><span class=\"goo\">ок</span><span class=\"skipped\">о</span><span class=\"goo\"> </span><span class=\"baaa\">\"</span><span class=\"skipped\">«</span><span class=\"goo\">Ел</span><span class=\"skipped\">к</span><span class=\"goo\">ек</span><span class=\"skipped\">е</span><span class=\"goo\">тро</span><span class=\"baaa\">н</span><span class=\"goo\">ний го</span><span class=\"skipped\">го</span><span class=\"goo\">дин</span><span class=\"baaa\">н</span><span class=\"goo\">ик</span><span class=\"baaa\">\".</span><span class=\"skipped\">»\n" +
                        "Студента __2__ курсу групи__КС-20__ \n" +
                        "спеціальності_121 “Інженерія програмного\n" +
                        "забезпечення(програмна інженерія)”_ </span><span class=\"goo\">\n" +
                        "</span><span class=\"skipped\">____________</span><span class=\"goo\">Вус </span><span class=\"skipped\"> </span><span class=\"goo\">Я.</span><span class=\"skipped\"> </span><span class=\"goo\">Я.</span><span class=\"skipped\">______________\n" +
                        "(прізвище та ініціали) </span><span class=\"goo\">\n" +
                        "</span><span class=\"skipped\">Керівник к.т.н. доц. </span><span class=\"goo\">Супруненко О.О.</span><span class=\"baaa\">\n" +
                        "</span>\n" +
                        "</p>");
            }
            workRepository.save(work);
        }
        discipline.setUpdating(false);
        disciplineRepository.save(discipline);
        notifier.notifyListeners(discipline.getId());
    }
}
