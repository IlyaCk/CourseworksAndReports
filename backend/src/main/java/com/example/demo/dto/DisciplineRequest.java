package com.example.demo.dto;

import com.example.demo.entity.enums.DisciplineType;
import com.example.demo.entity.enums.FileNameTemplate;
import com.example.demo.entity.enums.NameFormat;
import com.example.demo.entity.enums.PageNumberLocation;
import lombok.Data;

@Data
public class DisciplineRequest {
    private String name;
    private Integer year;
    private DisciplineType type;
    private PageNumberLocation pageNumberLocation;
    private NameFormat nameFormat;
    private String topicDistributionLink;
    private String googleClassLink;
    private String googleAssignmentLink;
    private FileNameTemplate[] template;
}