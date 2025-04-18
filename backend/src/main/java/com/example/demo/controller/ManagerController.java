package com.example.demo.controller;

import com.example.demo.dto.DisciplineRequest;
import com.example.demo.dto.UpdateDisciplineRequest;
import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.Work;
import com.example.demo.service.*;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.CourseWork;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/manager")
public class ManagerController {

    private final ManagerService managerService;
    private final GoogleClassroomService googleClassroomService;
    private final DisciplineService disciplineService;
    private final DepartmentService departmentService;
    private final BackgroundService backgroundService;
    private final DisciplineUpdateNotifier notifier;

    @GetMapping("/")
    public Department getDepartment(@AuthenticationPrincipal OAuth2User principal) {
        return managerService.getDepartment(principal.getAttribute("email"));
    }

    @Transactional
    @PostMapping("/disciplines")
    public ResponseEntity<Discipline> createDiscipline(@RequestBody DisciplineRequest request, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        Department department = managerService.getDepartment(authorizedClient.getPrincipalName());
        Discipline discipline = disciplineService.createDiscipline(authorizedClient.getAccessToken().getTokenValue(), request);
        department.getDisciplines().add(discipline);
        departmentService.saveDepartment(department);
        Set<Work> works = new HashSet<>(managerService.createWorks(authorizedClient.getAccessToken().getTokenValue(), discipline));
        discipline.setWorks(works);
        disciplineService.saveDiscipline(discipline);
        backgroundService.verifyWorks(authorizedClient.getAccessToken().getTokenValue(), discipline);
        return ResponseEntity.ok(discipline);
    }

    @PatchMapping("/disciplines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDiscipline(@PathVariable Long id, @RequestBody UpdateDisciplineRequest request) {
        disciplineService.updateDiscipline(id, request);
    }

    @GetMapping("/disciplines/{id}/wait-update")
    public DeferredResult<Boolean> waitUntilUpdated(@PathVariable Long id) {
        return notifier.registerListener(id);
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
