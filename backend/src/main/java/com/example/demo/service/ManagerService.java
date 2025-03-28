package com.example.demo.service;

import com.example.demo.entity.Department;
import com.example.demo.repository.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ManagerService {
    private final DepartmentRepository departmentRepository;

    public Department getDepartment(String email) {
        return departmentRepository.findByResponsibleUserEmail(email).orElse(null);
    }

    public void saveDepartment(Department department) {
        departmentRepository.save(department);
    }
}
