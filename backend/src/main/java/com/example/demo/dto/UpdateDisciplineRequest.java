package com.example.demo.dto;

import com.example.demo.entity.enums.DisciplineVisibility;
import com.example.demo.entity.enums.NameFormat;
import com.example.demo.entity.enums.PageNumberLocation;
import lombok.Data;

@Data
public class UpdateDisciplineRequest {
    private String name;
    private Integer year;
    private NameFormat nameFormat;
    private PageNumberLocation pageNumberLocation;
    private DisciplineVisibility visibility;
}