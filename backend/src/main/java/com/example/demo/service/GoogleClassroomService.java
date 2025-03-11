package com.example.demo.service;

import com.example.demo.entity.AttachmentDTO;
import com.example.demo.entity.CollectionDTO;
import com.example.demo.utils.PDFTools;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.classroom.Classroom;
import com.google.api.services.classroom.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoogleClassroomService {

    private static final String APPLICATION_NAME = "coursework-management";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private final GoogleDriveService googleDriveService;

    @Autowired
    public GoogleClassroomService(GoogleDriveService googleDriveService) {
        this.googleDriveService = googleDriveService;
    }

    private Classroom getClassroomService(String accessToken) throws GeneralSecurityException, IOException {
        return new Classroom.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public List<Course> getCourses(String accessToken) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        ListCoursesResponse response = classroomService.courses().list().execute();
        return response.getCourses();
    }

    public Course getCourse(String accessToken, String courseId) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        return classroomService.courses().get(courseId).execute();
    }

    public List<CourseWork> getCourseWorks(String accessToken, String courseId) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        return classroomService.courses().courseWork().list(courseId).execute().getCourseWork();
    }

    public CourseWork getCourseWork(String accessToken, String courseId, String cwId) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        return classroomService.courses().courseWork().get(courseId, cwId).execute();
    }

    public List<Student> getStudents(String accessToken, String courseId) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        List<Student> allStudents = new ArrayList<>();
        String pageToken = null;

        do {
            Classroom.Courses.Students.List request = classroomService.courses().students().list(courseId).setPageSize(100);
            if (pageToken != null) {
                request.setPageToken(pageToken);
            }

            ListStudentsResponse response = request.execute();
            if (response.getStudents() != null) {
                allStudents.addAll(response.getStudents());
            }

            pageToken = response.getNextPageToken();
        } while (pageToken != null);

        return allStudents;
    }

    public List<StudentSubmission> getSubmissions(String accessToken, String courseId, String cwId) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        List<StudentSubmission> submissions = classroomService.courses().courseWork().studentSubmissions().list(courseId, cwId).setStates(List.of("TURNED_IN", "RETURNED")).execute().getStudentSubmissions();
        List<Student> students = getStudents(accessToken, courseId);
        for (StudentSubmission submission : submissions) {
            for (Student student : students) {
                if (submission.getUserId().equals(student.getUserId())) {
                    submission.set("studentName", student.getProfile().getName().getFullName());
                    break;
                }
            }
            List<Attachment> attachments = submission.getAssignmentSubmission().getAttachments();
            if (attachments != null) {
                for (Attachment attachment : attachments) {
                    if (attachment.getDriveFile() != null && attachment.getDriveFile().getTitle().endsWith(".pdf")) {
                        boolean containsKeyword = PDFTools.checkPdfForKeyword(googleDriveService.getFileContent(accessToken, attachment.getDriveFile().getId()), "Курсова робота");
                        attachment.set("isCourseWork", containsKeyword);
                    }
                }
            }
        }
        return submissions;
    }

    public List<AttachmentDTO> getAttachmentsFromSubmissions(String accessToken, String courseId, String cwId, CollectionDTO collectionDTO) throws GeneralSecurityException, IOException {
        Classroom classroomService = getClassroomService(accessToken);
        List<StudentSubmission> submissions = classroomService.courses().courseWork().studentSubmissions().list(courseId, cwId).setStates(List.of("TURNED_IN")).execute().getStudentSubmissions();
        List<Student> students = getStudents(accessToken, courseId);
        List<AttachmentDTO> attachmentsList = new ArrayList<>();

        for (StudentSubmission submission : submissions) {
            String studentName = students.stream()
                    .filter(student -> submission.getUserId().equals(student.getUserId()))
                    .map(student -> student.getProfile().getName().getFullName())
                    .findFirst()
                    .orElse("Unknown");

            List<Attachment> attachments = submission.getAssignmentSubmission().getAttachments();
            if (attachments != null) {
                for (Attachment attachment : attachments) {
                    if (attachment.getDriveFile() != null &&
                            attachment.getDriveFile().getTitle() != null &&
                            googleDriveService.getFile(accessToken, attachment.getDriveFile().getId()).getMimeType().equals("application/pdf")) {
                        attachmentsList.add(new AttachmentDTO(
                                attachment.getDriveFile().getId(),
                                collectionDTO,
                                studentName,
                                attachment.getDriveFile().getTitle(),
                                attachment.getDriveFile().getAlternateLink()
                        ));
                    }
                }
            }
        }
        return attachmentsList;
    }
}
