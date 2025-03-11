package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Attachment")
public class AttachmentDTO {
    @Id
    private String id;
    private String studentName;
    private String title;
    private String link;
    private Boolean isCoursework;

    @ManyToOne
    private CollectionDTO collection;

    public AttachmentDTO() {
    }

    public AttachmentDTO(String id, CollectionDTO collection, String studentName, String title, String link) {
        this.id = id;
        this.collection = collection;
        this.studentName = studentName;
        this.title = title;
        this.link = link;
    }
}
