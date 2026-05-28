package com.example.demo.controller;

import com.example.demo.dto.DisciplineDTO;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.Work;
import com.example.demo.repository.DisciplineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    private final DisciplineRepository disciplineRepository;

    @GetMapping("/")
    public List<DisciplineDTO> getWorks(@AuthenticationPrincipal OAuth2User principal) {
        List<Discipline> disciplines = disciplineRepository.findByStudents_Email(principal.getName());
        return disciplines.stream()
                .map(discipline -> {
                    List<Work> filteredWorks = discipline.getWorks().stream()
                            .filter(work -> work.getStudent() != null && principal.getName().equals(work.getStudent().getEmail()))
                            .toList();
                    return new DisciplineDTO(discipline.getName(), discipline.getYear(), filteredWorks);
                })
                .filter(dto -> !dto.works.isEmpty())
                .toList();
    }
}
