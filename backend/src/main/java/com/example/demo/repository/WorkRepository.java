package com.example.demo.repository;

import com.example.demo.entity.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkRepository extends JpaRepository<Work, Long> {

    List<Work> getWorksByStudent_Email(String studentEmail);

    @Query("SELECT w FROM Work w WHERE str(w.id) LIKE CONCAT(:prefix, '%')")
    Page<Work> findByIdStartingWith(@Param("prefix") String prefix, Pageable pageable);
}
