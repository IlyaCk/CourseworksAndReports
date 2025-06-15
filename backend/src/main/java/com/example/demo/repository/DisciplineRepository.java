package com.example.demo.repository;

import com.example.demo.entity.Discipline;
import com.example.demo.entity.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DisciplineRepository extends JpaRepository<Discipline, Long> {
    List<Discipline> findByStudents_Email(String email);
    List<Discipline> findBySupervisors_Email(String email);

    @Query("SELECT d FROM Discipline d JOIN d.works w WHERE w.id = :workId")
    Discipline findByWorkId(@Param("workId") Long workId);

    Page<Discipline> findByNameContainingIgnoreCase(String name, Pageable pageable);
}