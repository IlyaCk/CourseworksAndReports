package com.example.demo.controller.data;

import com.example.demo.entity.Department;
import com.example.demo.repository.DepartmentRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data/departments")
public class DepartmentDataController {

    private final DepartmentRepository departmentRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDepartments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(name = "filter", required = false) String filterJson) {

        Map<String, Object> filters = new HashMap<>();
        if (filterJson != null) {
            try {
                filters = new ObjectMapper().readValue(filterJson, Map.class);
            } catch (Exception e) {
            }
        }

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, sort));

        Page<Department> departmentPage = departmentRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", departmentPage.getContent());
        response.put("total", departmentPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDepartment(@PathVariable Long id) {
        Optional<Department> departmentOpt = departmentRepository.findById(id);

        if (departmentOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", departmentOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createDepartment(@RequestBody Department department) {
        Department createdDepartment = departmentRepository.save(department);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdDepartment);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDepartment(@PathVariable Long id, @RequestBody Department department) {
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        department.setId(id);
        Department updatedDepartment = departmentRepository.save(department);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedDepartment);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        if (!departmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        departmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteDepartments(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                departmentRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
                // Ігноруємо помилки видалення неіснуючих записів
            }
        }
        return ResponseEntity.noContent().build();
    }
}