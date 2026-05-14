package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Data
@Entity
@Table(name = "departments")
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String ministry;
    private String HEI;
    private String cityYear;

    @OneToOne
    @JoinColumn(name = "responsible_user", referencedColumnName = "id")
    private User responsibleUser;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "department_heads",
            joinColumns = @JoinColumn(name = "department_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> headUsers;

    @OneToMany
    @JoinColumn(name = "department_id")
    private Set<Discipline> disciplines;
}
