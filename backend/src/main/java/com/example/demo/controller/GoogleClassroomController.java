package com.example.demo.controller;

import com.example.demo.service.GoogleClassroomService;
import com.google.api.services.classroom.model.Course;
import com.google.api.services.classroom.model.CourseWork;
import com.google.api.services.classroom.model.StudentSubmission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/classroom")
public class GoogleClassroomController {

    private final GoogleClassroomService googleClassroomService;

    @Autowired
    public GoogleClassroomController(GoogleClassroomService googleClassroomService) {
        this.googleClassroomService = googleClassroomService;
    }

    @GetMapping("/courses")
    public List<Course> getCourses(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return googleClassroomService.getCourses(accessToken.getTokenValue())
                .stream()
                .toList();
    }

    @GetMapping(value = "/courses/{courseId}")
    public Course getCourse(@PathVariable("courseId") String courseId,
                            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return googleClassroomService.getCourse(accessToken.getTokenValue(), courseId);
    }

    @GetMapping(value = "/courses/{courseId}/courseworks")
    public List<CourseWork> getCourseWorks(@PathVariable("courseId") String courseId,
                                           @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return googleClassroomService.getCourseWorks(accessToken.getTokenValue(), courseId);
    }

    @GetMapping(value = "/courses/{courseId}/courseworks/{courseWorkId}")
    public CourseWork getCourseWork(@PathVariable("courseId") String courseId, @PathVariable("courseWorkId") String courseWorkId,
                                    @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return googleClassroomService.getCourseWork(accessToken.getTokenValue(), courseId, courseWorkId);
    }

    @GetMapping(value = "/courses/{courseId}/courseworks/{courseWorkId}/submissions")
    public List<StudentSubmission> getSubmissions(@PathVariable("courseId") String courseId, @PathVariable("courseWorkId") String courseWorkId,
                                                  @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) throws GeneralSecurityException, IOException {
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        return googleClassroomService.getSubmissions(accessToken.getTokenValue(), courseId, courseWorkId);
    }
}
