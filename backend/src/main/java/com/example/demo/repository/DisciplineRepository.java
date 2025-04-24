package com.example.demo.repository;

import com.example.demo.entity.Discipline;
import com.example.demo.entity.User;
import com.example.demo.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
    List<Discipline> findByStudents_Email(String email);
    List<Discipline> findBySupervisors_Email(String email);
}