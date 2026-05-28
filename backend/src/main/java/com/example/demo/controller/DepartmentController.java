package com.example.demo.controller;

import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import com.example.demo.entity.enums.DisciplineVisibility;
import com.example.demo.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @GetMapping("/")
    public List<Department> getAllDepartments() {
        return departmentService.getAllDepartments();
    }

    @GetMapping("/{id}")
    public Department getDepartmentById(@PathVariable Long id) {
        Department department = departmentService.getDepartmentById(id).orElseThrow();
        HashSet<Discipline> disciplines = new HashSet<>(department.getDisciplines().stream().filter(discipline -> discipline.getVisibility() == DisciplineVisibility.PUBLIC).toList());
        department.setDisciplines(disciplines);
        return department;
    }
}
