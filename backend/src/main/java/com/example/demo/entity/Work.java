package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "works")
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String theme;
    private String classroomLink;
    private String fullTextLink;
    private String checkTextLink;

    private String googleSubmissionLink;

    private boolean isCorrectStudent;
    private boolean isCorrectSupervisor;
    private boolean isCorrectTheme;

    @Enumerated(EnumType.STRING)
    private DisciplineType type;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @OneToOne
    @JoinColumn(name = "plagiarism_report", referencedColumnName = "id")
    private PlagiarismReport plagiarismReport;
}
