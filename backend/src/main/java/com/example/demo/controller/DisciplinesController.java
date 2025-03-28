package com.example.demo.controller;

import com.example.demo.entity.Discipline;
import com.example.demo.service.DisciplineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/disciplines")
public class DisciplinesController {

    private final DisciplineService disciplineService;

    @GetMapping("/{id}")
    public Discipline getDiscipline(@PathVariable Long id) {
        return disciplineService.getDiscipline(id);
    }
}
