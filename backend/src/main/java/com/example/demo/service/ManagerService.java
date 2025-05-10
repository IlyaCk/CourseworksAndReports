package com.example.demo.service;

import com.example.demo.dto.ExportWorksRequest;
import com.example.demo.entity.*;
import com.example.demo.entity.enums.DisciplineType;
import com.example.demo.entity.enums.PlagiarismCheckStatus;
import com.example.demo.entity.enums.WorkState;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WorkRepository;
import com.example.demo.utils.PDFTools;
import com.google.api.services.classroom.model.Attachment;
import com.google.api.services.classroom.model.Student;
import com.google.api.services.classroom.model.StudentSubmission;
import com.google.api.services.drive.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final DepartmentRepository departmentRepository;
    private final GoogleClassroomService googleClassroomService;
    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final GoogleSheetsService googleSheetsService;
    private final GoogleDriveService googleDriveService;

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
                            work.setState(WorkState.NEW);
                            work.setPlagiarismCheckStatus(PlagiarismCheckStatus.NOT_CHECKED);
                            work.setStudent(student.orElse(null));
                            work.setClassroomLink(attachment.getDriveFile().getAlternateLink());
                            work.setGoogleSubmissionLink(submission.getAlternateLink());
                            work.setTopicDistributionLink(discipline.getTopicDistributionLink());
                            work.setType(discipline.getType());
                            work.setTurnInDate(OffsetDateTime.parse(submission.getSubmissionHistory().reversed().stream()
                                    .filter(el -> el.getStateHistory().getState().equals("TURNED_IN"))
                                    .findFirst().get().getStateHistory().getStateTimestamp())
                                    .atZoneSameInstant(ZoneId.of("Europe/Kyiv")).toLocalDateTime());
                            assignmentRecord.ifPresent(record -> {
                                work.setTheme(record.topic());
                                work.setRawSupervisorName(record.supervisor());
                                work.setRawStudentName(record.student());
                                Optional<User> supervisor = supervisors.stream()
                                        .filter(user -> PDFTools.isNameMentioned(user.getName(), record.supervisor()))
                                        .findFirst();
                                work.setSupervisor(supervisor.orElse(null));
                                if (work.getType() == DisciplineType.QUALIFICATION_WORK){
                                    Optional<User> reviewer = supervisors.stream()
                                            .filter(user -> PDFTools.isNameMentioned(user.getName(), record.reviewer()))
                                            .findFirst();
                                    work.setReviewer(reviewer.orElse(null));
                                }
                                work.setStudentGroup(record.group());
                            });
                            workList.add(work);
                            break;
                        }
                    }
                }
            }
        }
        return workList;
    }

    private Optional<GoogleSheetsService.AssignmentRecord> findMatchingAssignment(User student, List<GoogleSheetsService.AssignmentRecord> assignments) {
        return assignments.stream()
                .filter(record -> PDFTools.isNameMentioned(student.getName(), record.student()))
                .findFirst();
    }

    public Work getWork(Long id) {
        return workRepository.findById(id).orElse(null);
    }

    public byte[] exportWorksAsZip(List<Long> ids, boolean includeFull, boolean includeShort, String accessToken) throws IOException, GeneralSecurityException {
        List<Work> works = workRepository.findAllById(ids);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (Work work : works) {
            if (includeFull && work.getFullTextLink() != null) {
                String fileId = GoogleDriveService.extractFileIdFromLink(work.getFullTextLink());
                File metadata = googleDriveService.getFileMetadata(accessToken, fileId);
                InputStream input = googleDriveService.getFileContent(accessToken, work.getFullTextLink());
                zipOut.putNextEntry(new ZipEntry(metadata.getName()));
                input.transferTo(zipOut);
                zipOut.closeEntry();
            }

            if (includeShort && work.getShortTextLink() != null) {
                String fileId = GoogleDriveService.extractFileIdFromLink(work.getShortTextLink());
                File metadata = googleDriveService.getFileMetadata(accessToken, fileId);
                InputStream input = googleDriveService.getFileContent(accessToken, work.getFullTextLink());
                zipOut.putNextEntry(new ZipEntry(metadata.getName()));
                input.transferTo(zipOut);
                zipOut.closeEntry();
            }
        }

        zipOut.close();
        return baos.toByteArray();
    }
}
