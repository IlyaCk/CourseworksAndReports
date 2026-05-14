package com.example.demo.controller;

import com.example.demo.entity.Discipline;
import com.example.demo.service.DisciplineService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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
