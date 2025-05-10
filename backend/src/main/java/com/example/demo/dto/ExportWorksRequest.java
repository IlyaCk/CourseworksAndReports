package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExportWorksRequest {
    private List<Long> ids;
    private boolean includeFull;
    private boolean includeShort;
}
