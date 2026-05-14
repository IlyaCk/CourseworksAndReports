package com.example.demo.controller.data;

import com.example.demo.entity.PlagiarismReport;
import com.example.demo.entity.Work;
import com.example.demo.repository.PlagiarismReportRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/data/plagiarism-reports")
public class PlagiarismReportDataController {

    private final PlagiarismReportRepository plagiarismReportRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllPlagiarismReports(
            @RequestParam(required = false) String ids,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int perPage,
            @RequestParam(defaultValue = "id") String sort,
            @RequestParam(defaultValue = "ASC") String order,
            @RequestParam(name = "filter", required = false) String filterJson) {
        Map<String, Object> filters = new HashMap<>();
        if (filterJson != null) {
            try {
                filters = new ObjectMapper().readValue(filterJson, Map.class);
            } catch (Exception e) {
            }
        }

        if (ids != null && !ids.isEmpty()) {
            String[] idArray = ids.split(",");
            List<Long> idList = Arrays.stream(idArray)
                    .map(Long::parseLong)
                    .toList();

            List<PlagiarismReport> plagiarismReports = plagiarismReportRepository.findAllById(idList);

            Map<String, Object> response = new HashMap<>();
            response.put("data", plagiarismReports);
            response.put("total", plagiarismReports.size());
            return ResponseEntity.ok(response);
        }

        String[] sortFields = sort.split(",");
        String actualSortField = sortFields[0];

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, actualSortField));

        Page<PlagiarismReport> reportPage = plagiarismReportRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", reportPage.getContent());
        response.put("total", reportPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlagiarismReport(@PathVariable Long id) {
        Optional<PlagiarismReport> reportOpt = plagiarismReportRepository.findById(id);

        if (reportOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", reportOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createPlagiarismReport(@RequestBody PlagiarismReport report) {
        PlagiarismReport createdReport = plagiarismReportRepository.save(report);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdReport);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updatePlagiarismReport(@PathVariable Long id, @RequestBody PlagiarismReport report) {
        if (!plagiarismReportRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        report.setId(id);
        PlagiarismReport updatedReport = plagiarismReportRepository.save(report);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedReport);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlagiarismReport(@PathVariable Long id) {
        if (!plagiarismReportRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        plagiarismReportRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deletePlagiarismReports(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                plagiarismReportRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
            }
        }
        return ResponseEntity.noContent().build();
    }
}