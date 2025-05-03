package com.example.demo.entity;

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

    @Enumerated(EnumType.STRING)
    private MatchLevel isCorrectStudent;

    @Enumerated(EnumType.STRING)
    private MatchLevel isCorrectSupervisor;

    @Enumerated(EnumType.STRING)
    private MatchLevel isCorrectTheme;

    @Enumerated(EnumType.STRING)
    private DisciplineType type;

    @ManyToOne
    @JoinColumn(name = "student_id")
    private User student;
    private String rawStudentName;

    @ManyToOne
    @JoinColumn(name = "supervisor_id")
    private User supervisor;
    private String rawSupervisorName;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    private User reviewer;

    @OneToOne
    @JoinColumn(name = "plagiarism_report", referencedColumnName = "id")
    private PlagiarismReport plagiarismReport;

    @Column(columnDefinition="TEXT")
    private String themeDifference;

    @Column(columnDefinition="TEXT")
    private String studentDifference;

    @Column(columnDefinition="TEXT")
    private String supervisorDifference;

    @Column(columnDefinition="TEXT")
    private String ministryDifference;

    @Column(columnDefinition="TEXT")
    private String HEIDifference;

    @Column(columnDefinition="TEXT")
    private String departmentDifference;

    @Column(columnDefinition="TEXT")
    private String groupDifference;

    @Column(columnDefinition="TEXT")
    private String cityYearDifference;
}
