package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "disciplines")
public class Discipline {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer year;
    private String topicDistributionLink;

    private String googleClassId;
    private String googleClassLink;

    private String googleAssignmentId;
    private String googleAssignmentLink;

    private boolean isUpdating;

    @Enumerated(EnumType.STRING)
    private DisciplineType type;

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
