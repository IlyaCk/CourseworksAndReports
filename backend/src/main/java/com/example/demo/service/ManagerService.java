package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.User;
import com.example.demo.entity.Work;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkRepository;
import com.example.demo.utils.PDFTools;
import com.google.api.services.classroom.model.Attachment;
import com.google.api.services.classroom.model.Student;
import com.google.api.services.classroom.model.StudentSubmission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final DepartmentRepository departmentRepository;
    private final GoogleClassroomService googleClassroomService;
    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final GoogleSheetsService googleSheetsService;

    public Department getDepartment(String email) {
        return departmentRepository.findByResponsibleUserEmail(email).orElse(null);
    }

    public List<Work> createWorks(String accessToken, Discipline discipline) throws GeneralSecurityException, IOException {
        List<StudentSubmission> submissions = googleClassroomService.getSubmissions(accessToken, discipline.getGoogleClassId(), discipline.getGoogleAssignmentId());
        List<Student> students = googleClassroomService.getStudents(accessToken, discipline.getGoogleClassId());
        List<Work> workList = new ArrayList<>();
        List<GoogleSheetsService.AssignmentRecord> assignments = googleSheetsService.extractAssignments(accessToken, discipline.getTopicDistributionLink());
        Set<User> supervisors = new HashSet<>(discipline.getSupervisors());

        for (StudentSubmission submission : submissions) {
            String studentEmail = students.stream()
                    .filter(student -> submission.getUserId().equals(student.getUserId()))
                    .map(student -> student.getProfile().getEmailAddress())
                    .findFirst()
                    .orElse("Unknown");
            Optional<User> student = userRepository.findByEmail(studentEmail);
            if (student.isPresent()) {
                Optional<GoogleSheetsService.AssignmentRecord> assignmentRecord = findMatchingAssignment(student.get(), assignments);
                List<Attachment> attachments = submission.getAssignmentSubmission().getAttachments();
                if (attachments != null) {
                    for (Attachment attachment : attachments) {
                        if (attachment.getDriveFile() != null && attachment.getDriveFile().getTitle() != null && attachment.getDriveFile().getTitle().endsWith(".pdf")) {
                            Work work = new Work();
                            work.setStudent(student.orElse(null));
                            work.setClassroomLink(attachment.getDriveFile().getAlternateLink());
                            work.setGoogleSubmissionLink(submission.getAlternateLink());
                            work.setType(discipline.getType());
                            assignmentRecord.ifPresent(record -> {
                                work.setTheme(record.topic());
                                Optional<User> supervisor = supervisors.stream()
                                        .filter(user -> PDFTools.isNameMentioned(user.getName(), record.supervisor()))
                                        .findFirst();
                                work.setSupervisor(supervisor.orElse(null));
                            });
                            workList.add(work);
                            break;
                        }
                    }
                }
            }
        }
        return workRepository.saveAll(workList);
    }

    private Optional<GoogleSheetsService.AssignmentRecord> findMatchingAssignment(User student, List<GoogleSheetsService.AssignmentRecord> assignments) {
        return assignments.stream()
                .filter(record -> PDFTools.isNameMentioned(student.getName(), record.student()))
                .findFirst();
    }
}
