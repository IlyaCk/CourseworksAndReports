package com.example.demo.controller;

import com.example.demo.entity.Department;
import com.example.demo.entity.Work;
import com.example.demo.repository.DepartmentRepository;
import com.example.demo.repository.WorkRepository;
import com.example.demo.service.ManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
@RequestMapping("/api/works")
@RequiredArgsConstructor
public class WorkController {

    private final WorkRepository workRepository;
    private final DepartmentRepository departmentRepository;

    @GetMapping("/{id}")
    public Work getWork(@PathVariable Long id, @AuthenticationPrincipal OAuth2User principal) {
        Work work = workRepository.findById(id).orElseThrow();
        Department department = departmentRepository.findByResponsibleUserEmail(principal.getName()).orElse(null);
        if ((work.getStudent() != null && work.getStudent().getEmail().equals(principal.getName())) ||
                (work.getSupervisor() != null && work.getSupervisor().getEmail().equals(principal.getName())) ||
                department != null
        ) {
            return work;
        }
        return null;
    }
}
