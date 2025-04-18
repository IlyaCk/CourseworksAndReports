package com.example.demo.service;

import com.example.demo.dto.UpdateDisciplineRequest;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import com.google.api.services.classroom.model.Student;
import com.google.api.services.classroom.model.Teacher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.demo.dto.DisciplineRequest;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.User;
import com.example.demo.repository.DisciplineRepository;
import com.example.demo.repository.UserRepository;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class DisciplineService {
    private final DisciplineRepository disciplineRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final GoogleClassroomService googleClassroomService;

    public Discipline getDiscipline(Long id) {
        return disciplineRepository.findById(id).orElse(null);
    }

    public Discipline saveDiscipline(Discipline discipline) {
        return disciplineRepository.save(discipline);
    }

    public void updateDiscipline(Long id, UpdateDisciplineRequest request) {
        Discipline discipline = disciplineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discipline not found"));

        discipline.setName(request.getName());
        discipline.setYear(request.getYear());

        disciplineRepository.save(discipline);
    }

    public Discipline createDiscipline(String accessToken, DisciplineRequest request) throws GeneralSecurityException, IOException {
        Discipline discipline = new Discipline();
        discipline.setName(request.getName());
        discipline.setYear(request.getYear());
        discipline.setTopicDistributionLink(request.getTopicDistributionLink());
        discipline.setGoogleClassId(request.getGoogleClassId());
        discipline.setGoogleClassLink(googleClassroomService.getCourse(accessToken, request.getGoogleClassId()).getAlternateLink());
        discipline.setGoogleAssignmentId(request.getGoogleAssignmentId());
        discipline.setGoogleAssignmentLink(googleClassroomService.getCourseWork(accessToken, request.getGoogleClassId(), request.getGoogleAssignmentId()).getAlternateLink());
        discipline.setType(request.getType());

        List<Student> googleStudents = googleClassroomService.getStudents(accessToken, request.getGoogleClassId());
        List<Teacher> googleTeachers = googleClassroomService.getTeachers(accessToken, request.getGoogleClassId());
        Set<User> students = new HashSet<>();
        Set<User> supervisors = new HashSet<>();

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

        discipline.setStudents(students);

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
        discipline.setSupervisors(supervisors);
        discipline.setWorks(Set.of());
        discipline.setUpdating(true);

        return disciplineRepository.save(discipline);
    }
}
