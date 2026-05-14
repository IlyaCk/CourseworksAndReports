package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@AllArgsConstructor
@Data
public class ExportWorksRequest {
    private List<Long> ids;
    private boolean includeFull;
    private boolean includeShort;
    private boolean includeFullReport;
    private boolean includeShortReport;
}
