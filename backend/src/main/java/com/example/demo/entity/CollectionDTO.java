package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "collection")
public class CollectionDTO {
    @Id
    private String id;
    private String name;
    private String taskId;
    private Boolean isUpdating;
    private LocalDateTime updatedAt;

    public CollectionDTO() {
    }

    public CollectionDTO(String id, String name, String taskId, Boolean isUpdating) {
        this.id = id;
        this.name = name;
        this.taskId = taskId;
        this.isUpdating = isUpdating;
        this.updatedAt = LocalDateTime.now();
    }
}
