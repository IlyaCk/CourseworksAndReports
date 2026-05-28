package com.example.demo.dto;

import com.example.demo.entity.Work;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DisciplineDTO {
    public String name;
    public Integer year;
    public List<Work> works;
}
