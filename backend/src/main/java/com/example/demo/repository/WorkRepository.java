package com.example.demo.repository;

import com.example.demo.entity.Work;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    List<Work> getWorksByStudent_Email(String studentEmail);
}
