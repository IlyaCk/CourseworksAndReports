package com.example.demo.repository;

import com.example.demo.entity.Department;
import com.example.demo.entity.Discipline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {
    Optional<Department> findByResponsibleUserEmail(String email);

    Optional<Department> findByDisciplinesContains(Set<Discipline> disciplines);
}