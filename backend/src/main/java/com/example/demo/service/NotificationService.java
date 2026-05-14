package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.entity.enums.NotificationType;
import com.example.demo.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void save(User recipient, NotificationType type, String message, String url) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setTargetUrl(url);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    public void createWorkCreatedNotification(Work work, Discipline discipline, Department department) {
        String studentName = work.getStudent().getName();
        String disciplineName = discipline.getName();

        save(
                work.getStudent(),
                NotificationType.WORK_CREATED,
                String.format("Ваша робота у дисципліні \"%s\" додана до системи.", disciplineName),
                "/student"
        );

        if (work.getSupervisor() != null) {
            save(
                    work.getSupervisor(),
                    NotificationType.WORK_CREATED,
                    String.format("Додано роботу студента %s у дисципліні \"%s\".", studentName, disciplineName),
                    "/supervisor"
            );
        }

        save(
                department.getResponsibleUser(),
                NotificationType.WORK_CREATED,
                String.format("У дисципліні \"%s\" додано роботу студента %s.", disciplineName, studentName),
                "/manager/disciplines/" + discipline.getId()
        );

    }

    public void createWorkUpdatedNotification(Work work, Discipline discipline, Department department) {
        String studentName = work.getStudent().getName();
        String disciplineName = discipline.getName();

        save(
                work.getStudent(),
                NotificationType.WORK_UPDATED,
                String.format("Вашу роботу в дисципліні \"%s\" оновлено.", disciplineName),
                "/student"
        );

        if (work.getSupervisor() != null) {
            save(
                    work.getSupervisor(),
                    NotificationType.WORK_UPDATED,
                    String.format("Роботу студента %s у дисципліні \"%s\" оновлено.", studentName, disciplineName),
                    "/supervisor"
            );
        }

        save(
                department.getResponsibleUser(),
                NotificationType.WORK_UPDATED,
                String.format("Оновлено роботу студента %s у дисципліні \"%s\".", studentName, disciplineName),
                "/manager/disciplines/" + discipline.getId()
        );
    }

    public void createCheckResultNotification(Work work, Discipline discipline) {
        String disciplineName = discipline.getName();
        String studentName = work.getStudent().getName();

        save(
                work.getStudent(),
                NotificationType.CHECK_RESULT,
                String.format("Натисніть щоб переглянути результати базових перевірок роботи у дисципліні \"%s\".", disciplineName),
                "/works/" + work.getId()
        );

        if (work.getSupervisor() != null) {
            save(
                    work.getSupervisor(),
                    NotificationType.CHECK_RESULT,
                    String.format("Натисніть щоб переглянути результати базових перевірок роботи студента %s у \"%s\"", studentName, disciplineName),
                    "/works/" + work.getId()
            );
        }
    }

    public void createUnderReviewNotification(Work work, Discipline discipline, Department department) {
        String disciplineName = discipline.getName();
        String studentName = work.getStudent().getName();

        save(
                work.getStudent(),
                NotificationType.UNDER_REVIEW,
                String.format("Ваша робота у \"%s\" перебуває на перевірці на текстові запозичення. Завантажувати нову версію заборонено!", disciplineName),
                "/student"
        );

        if (work.getSupervisor() != null) {
            save(
                    work.getSupervisor(),
                    NotificationType.UNDER_REVIEW,
                    String.format("Роботу студента %s у дисципліні \"%s\" забрано на перевірку.", studentName, disciplineName),
                    "/supervisor"
            );
        }

        save(
                department.getResponsibleUser(),
                NotificationType.UNDER_REVIEW,
                String.format("Роботу студента %s у дисципліні \"%s\" забрано на перевірку.", studentName, disciplineName),
                "/manager/disciplines/" + discipline.getId()
        );
    }

    public void createReportAddedNotification(Work work, Discipline discipline, Department department) {
        String disciplineName = discipline.getName();
        String studentName = work.getStudent().getName();

        save(
                work.getStudent(),
                NotificationType.REPORT_ADDED,
                String.format("До вашої роботи у \"%s\" додано звіт перевірки на текстові запозичення.", disciplineName),
                "/student"
        );

        if (work.getSupervisor() != null) {
            save(
                    work.getSupervisor(),
                    NotificationType.REPORT_ADDED,
                    String.format("До роботи студента %s у \"%s\" додано звіт перевірки.", studentName, disciplineName),
                    "/supervisor"
            );
        }

        save(
                department.getResponsibleUser(),
                NotificationType.REPORT_ADDED,
                String.format("До роботи студента %s у дисципліні \"%s\" додано звіт перевірки.", studentName, disciplineName),
                "/manager/disciplines/" + discipline.getId()
        );

    }

    public void createWorkAbortedNotification(Work work, Discipline discipline, Department department) {
        String disciplineName = discipline.getName();

        save(
                work.getStudent(),
                NotificationType.WORK_ABORTED,
                String.format("Оновлення Вашої роботи у \"%s\" відхилено, оскільки поточна версія перебуває в перевірці або вже перевірена", disciplineName),
                "/student"
        );

        if (work.getSupervisor() != null) {
            save(
                    work.getSupervisor(),
                    NotificationType.WORK_ABORTED,
                    String.format("Оновлення роботи студента %s у \"%s\" відхилено, оскільки поточна версія перебуває в перевірці або вже перевірена", work.getStudent().getName(), disciplineName),
                    "/supervisor"
            );
        }

        save(
                department.getResponsibleUser(),
                NotificationType.WORK_ABORTED,
                String.format("Оновлення роботи студента %s у \"%s\" відхилено, оскільки поточна версія перебуває в перевірці або вже перевірена", work.getStudent().getName(), disciplineName),
                "/manager/disciplines/" + discipline.getId()
        );
    }
}
