package com.example.demo.controller;

import com.example.demo.dto.DisciplineRequest;
import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.service.DisciplineService;
import com.example.demo.service.GoogleClassroomService;
import com.example.demo.service.ManagerService;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.CourseWork;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerController {

    private final ManagerService managerService;
    private final GoogleClassroomService googleClassroomService;
    private final DisciplineService disciplineService;

    @GetMapping("/")
    public Department getDepartment(@AuthenticationPrincipal OAuth2User principal) {
        return managerService.getDepartment(principal.getAttribute("email"));
    }

    @PostMapping("/disciplines")
    public ResponseEntity<Discipline> createDiscipline(@RequestBody DisciplineRequest request, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        Department department = managerService.getDepartment(authorizedClient.getPrincipalName());
        Discipline discipline = disciplineService.createDiscipline(authorizedClient.getAccessToken().getTokenValue(), request);
        department.getDisciplines().add(discipline);
        managerService.saveDepartment(department);
        return ResponseEntity.ok(discipline);
    }

    @GetMapping("/classrooms")
    public List<Course> getClassrooms(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient)
            throws GeneralSecurityException, IOException {
        List<Course> allCourses = googleClassroomService.getCourses(authorizedClient.getAccessToken().getTokenValue());
        Pattern pattern = Pattern.compile("курсов[а-я]*|квал[а-я]*", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
        return allCourses.stream()
                .filter(course -> pattern.matcher(course.getName()).find())
                .collect(Collectors.toList());
    }

    @GetMapping("/{course}/courseworks")
    public List<CourseWork> getCourseWorks(@PathVariable("course") String course, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        return googleClassroomService.getCourseWorks(authorizedClient.getAccessToken().getTokenValue(), course);
    }
}
