package com.example.demo.dto;

import com.example.demo.entity.DisciplineType;
import lombok.Data;

@Data
public class DisciplineRequest {
    private String name;
    private Integer year;
    private String topicDistributionLink;
    private String googleClassId;
    private String googleAssignmentId;
    private DisciplineType type;
}
