package com.example.demo.entity;

import com.example.demo.entity.enums.DisciplineType;
import com.example.demo.entity.enums.DisciplineVisibility;
import com.example.demo.entity.enums.NameFormat;
import com.example.demo.entity.enums.PageNumberLocation;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Entity
@Table(name = "disciplines")
public class Discipline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String fileName;
    private String nameAtTitlePage;
    private Integer year;
    private String topicDistributionLink;

    private String googleClassId;
    private String googleClassLink;

    private String googleAssignmentId;
    private String googleAssignmentLink;

    private String googleDriveFolderLink;

    private LocalDateTime updateDate;
    private String fileNameTemplate;

    private boolean isUpdating;

    @Enumerated(EnumType.STRING)
    private DisciplineType type;

    @Enumerated(EnumType.STRING)
    private NameFormat nameFormat;

    @Enumerated(EnumType.STRING)
    private PageNumberLocation pageNumberLocation;

    @Enumerated(EnumType.STRING)
    private DisciplineVisibility visibility;

    @ManyToMany
    @JoinTable(
            name = "discipline_students",
            joinColumns = @JoinColumn(name = "discipline_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> students;

    @ManyToMany
    @JoinTable(
            name = "discipline_supervisors",
            joinColumns = @JoinColumn(name = "discipline_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> supervisors;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "discipline_id")
    private Set<Work> works;

    @PreRemove
    private void preRemove() {
        if (students != null) {
            students.clear();
        }
        if (supervisors != null) {
            supervisors.clear();
        }
    }
}
