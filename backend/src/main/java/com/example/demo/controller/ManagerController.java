package com.example.demo.controller;

import com.example.demo.dto.DisciplineRequest;
import com.example.demo.dto.UpdateDisciplineRequest;
import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.Work;
import com.example.demo.repository.DisciplineRepository;
import com.example.demo.service.*;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.CourseWork;
import com.google.api.services.classroom.model.CourseWorkMaterial;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
import java.util.Map;
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
    private final DisciplineRepository disciplineRepository;
    private final GoogleDriveService googleDriveService;

    @GetMapping("/")
    public Department getDepartment(@AuthenticationPrincipal OAuth2User principal) {
        return managerService.getDepartment(principal.getAttribute("email"));
    }

    @Transactional
    @PostMapping("/disciplines")
    public Discipline createDiscipline(@RequestBody DisciplineRequest request, @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        Department department = managerService.getDepartment(authorizedClient.getPrincipalName());
        department.getDisciplines().forEach(discipline -> {
            if (discipline.getName().equals(request.getName()) && discipline.getYear().equals(request.getYear())) {
                throw new RuntimeException("Discipline already exists");
            }
        });
        Discipline discipline = disciplineService.createDiscipline(authorizedClient.getAccessToken().getTokenValue(), request);
        Set<Work> works = new HashSet<>(managerService.createWorks(authorizedClient.getAccessToken().getTokenValue(), discipline));
        discipline.setWorks(works);
        disciplineRepository.save(discipline);
        department.getDisciplines().add(discipline);
        departmentService.saveDepartment(department);
        backgroundService.verifyWorks(authorizedClient.getAccessToken().getTokenValue(), discipline, department);
        return discipline;
    }

    @PatchMapping("/disciplines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateDiscipline(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @PathVariable Long id, @RequestBody UpdateDisciplineRequest request) throws GeneralSecurityException, IOException {
        disciplineService.updateDiscipline(authorizedClient.getAccessToken().getTokenValue(), id, request);
    }

    @DeleteMapping("/disciplines/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDiscipline(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient, @PathVariable Long id) throws GeneralSecurityException, IOException {
        Discipline discipline = disciplineRepository.findById(id).orElseThrow(() -> new RuntimeException("Discipline not found"));
        if (discipline.getGoogleDriveFolderLink() != null) {
            googleDriveService.deleteFile(authorizedClient.getAccessToken().getTokenValue(), GoogleDriveService.extractFolderIdFromLink(discipline.getGoogleDriveFolderLink()));
        }
        disciplineRepository.delete(discipline);
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

    @PostMapping("/courseworks")
    public List<CourseWork> getCourseWorksByLink(
            @RequestBody Map<String, String> body,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        return googleClassroomService.getCourseWorks(authorizedClient.getAccessToken().getTokenValue(), body.get("courseLink"));
    }

    @PostMapping("/materials")
    public List<CourseWorkMaterial> getMaterialsByLink(
            @RequestBody Map<String, String> body,
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        return googleClassroomService.getCourseMaterials(authorizedClient.getAccessToken().getTokenValue(), body.get("courseLink"));
    }


    @GetMapping("/works/{id}")
    public Work getWork(@PathVariable Long id) {
        return managerService.getWork(id);
    }
}
