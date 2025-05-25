package com.example.demo.service;

import com.example.demo.dto.UpdateDisciplineRequest;
import com.example.demo.entity.*;
import com.example.demo.entity.enums.DisciplineVisibility;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.RoleRepository;
import com.google.api.services.classroom.model.Student;
import com.google.api.services.classroom.model.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.dto.DisciplineRequest;
import com.example.demo.repository.DisciplineRepository;
import com.example.demo.repository.UserRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DisciplineService {
    private final DisciplineRepository disciplineRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GoogleClassroomService googleClassroomService;
    private final GoogleDriveService googleDriveService;

    public Discipline getDiscipline(Long id) {
        return disciplineRepository.findById(id).orElse(null);
    }

    public Discipline saveDiscipline(Discipline discipline) {
        return disciplineRepository.save(discipline);
    }

    public void updateDiscipline(String accessToken, Long id, UpdateDisciplineRequest request) throws GeneralSecurityException, IOException {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discipline not found"));

        Department department = departmentRepository.findByDisciplinesContains(Set.of(discipline))
                .orElseThrow(() -> new RuntimeException("Department not found"));

        department.getDisciplines().forEach(d -> {
            if (d.getName().equals(request.getName()) && d.getYear().equals(request.getYear()) && !Objects.equals(d.getId(), id)) {
                throw new RuntimeException("Discipline already exists");
            }
        });
        if (!Objects.equals(request.getName(), discipline.getName()) ||
                !Objects.equals(request.getYear(), discipline.getYear())) {
            String newFolderId = googleDriveService.renameFile(accessToken, GoogleDriveService.extractFolderIdFromLink(discipline.getGoogleDriveFolderLink()), request.getName() + "-" + request.getYear());
            discipline.setGoogleDriveFolderLink(
                    "https://drive.google.com/drive/folders/" + newFolderId
            );
        }
        discipline.setName(request.getName());
        discipline.setNameAtTitlePage(request.getNameAtTitlePage());
        discipline.setYear(request.getYear());
        discipline.setNameFormat(request.getNameFormat());
        discipline.setPageNumberLocation(request.getPageNumberLocation());
        discipline.setVisibility(request.getVisibility());

        if (discipline.getVisibility() == DisciplineVisibility.PUBLIC) {
            for (Work work : discipline.getWorks()) {
                if (work.getFullTextLink() != null){
                    googleDriveService.makeFilePublic(accessToken, GoogleDriveService.extractFileIdFromLink(work.getFullTextLink()));
                }
                if (work.getPlagiarismReport() != null && work.getPlagiarismReport().getShortReportLink() != null){
                    googleDriveService.makeFilePublic(accessToken, GoogleDriveService.extractFileIdFromLink(work.getPlagiarismReport().getShortReportLink()));
                }
            }
        }

        disciplineRepository.save(discipline);
    }

    public Discipline createDiscipline(String accessToken, DisciplineRequest request) throws GeneralSecurityException, IOException {
        Discipline discipline = new Discipline();
        discipline.setName(request.getName());
        discipline.setFileName(request.getFileName());
        discipline.setYear(request.getYear());
        discipline.setTopicDistributionLink(request.getTopicDistributionLink());
        String classId = googleClassroomService.getCourseId(accessToken, request.getGoogleClassLink());
        discipline.setGoogleClassId(classId);
        discipline.setGoogleClassLink(request.getGoogleClassLink());
        discipline.setGoogleAssignmentId(googleClassroomService.getCourseWorkId(accessToken, classId, request.getGoogleAssignmentLink()));
        discipline.setGoogleAssignmentLink(request.getGoogleAssignmentLink());
        discipline.setType(request.getType());
        discipline.setPageNumberLocation(request.getPageNumberLocation());
        discipline.setNameFormat(request.getNameFormat());
        discipline.setVisibility(DisciplineVisibility.PRIVATE);
        discipline.setFileNameTemplate(Arrays.stream(request.getTemplate()).map(Enum::name).collect(Collectors.joining("_")));
        discipline.setStudents(getStudents(accessToken, classId));
        discipline.setSupervisors(getSupervisors(accessToken, classId));
        discipline.setWorks(Set.of());
        discipline.setUpdating(true);
        return discipline;
    }

    public Discipline updateDisciplineUsers(String accessToken, Discipline discipline) throws GeneralSecurityException, IOException {
        String classId = discipline.getGoogleClassId();
        discipline.setGoogleClassId(classId);
        discipline.setStudents(getStudents(accessToken, classId));
        discipline.setSupervisors(getSupervisors(accessToken, classId));
        discipline.setUpdating(true);
        return discipline;
    }

    private Set<User> getStudents(String accessToken, String classId) throws GeneralSecurityException, IOException {
        List<Student> googleStudents = googleClassroomService.getStudents(accessToken, classId);
        Set<User> students = new HashSet<>();

        for (Student student : googleStudents) {
            User existingUser = userRepository.findByEmail(student.getProfile().getEmailAddress())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(student.getProfile().getEmailAddress());
                        newUser.setName(student.getProfile().getName().getFullName());
                        newUser.setRoles(Set.of(roleRepository.findByName("STUDENT")));
                        return userRepository.save(newUser);
                    });

            Set<Role> updatedRoles = new HashSet<>(existingUser.getRoles());
            updatedRoles.add(roleRepository.findByName("STUDENT"));
            existingUser.setRoles(updatedRoles);
            userRepository.save(existingUser);

            students.add(existingUser);
        }
        return students;
    }

    private Set<User> getSupervisors(String accessToken, String classId) throws GeneralSecurityException, IOException {
        List<Teacher> googleTeachers = googleClassroomService.getTeachers(accessToken, classId);
        Set<User> supervisors = new HashSet<>();

        for (Teacher teacher : googleTeachers) {
            User existingUser = userRepository.findByEmail(teacher.getProfile().getEmailAddress())
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(teacher.getProfile().getEmailAddress());
                        newUser.setName(teacher.getProfile().getName().getFullName());
                        newUser.setRoles(Set.of(roleRepository.findByName("SUPERVISOR")));
                        return userRepository.save(newUser);
                    });

            Set<Role> updatedRoles = new HashSet<>(existingUser.getRoles());
            updatedRoles.add(roleRepository.findByName("SUPERVISOR"));
            existingUser.setRoles(updatedRoles);
            userRepository.save(existingUser);

            supervisors.add(existingUser);
        }
        return supervisors;
    }
}
