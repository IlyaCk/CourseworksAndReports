package com.example.demo.controller.data;

import com.example.demo.entity.User;
import com.example.demo.entity.Work;
import com.example.demo.repository.WorkRepository;
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
@RequestMapping("/api/data/works")
public class WorkDataController {

    private final WorkRepository workRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllWorks(
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
            List<Long> idList = new ArrayList<>();
            Arrays.stream(idArray)
                    .forEach(id -> {
                        try {
                            idList.add(Long.parseLong(id));
                        } catch (NumberFormatException e) {
                        }
                    });
            List<Work> works = workRepository.findAllById(idList);

            Map<String, Object> response = new HashMap<>();
            response.put("data", works);
            response.put("total", works.size());
            return ResponseEntity.ok(response);
        }

        String[] sortFields = sort.split(",");
        String actualSortField = sortFields[0];

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, actualSortField));

        Page<Work> workPage;
        if (filters.containsKey("q")) {
            String query = (String) filters.get("q");
                workPage = workRepository.findByIdStartingWith(query, pageable);
        }
        else {
            workPage = workRepository.findAll(pageable);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("data", workPage.getContent());
        response.put("total", workPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getWork(@PathVariable Long id) {
        Optional<Work> workOpt = workRepository.findById(id);

        if (workOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", workOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createWork(@RequestBody Work work) {
        Work createdWork = workRepository.save(work);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdWork);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateWork(@PathVariable Long id, @RequestBody Work work) {
        if (!workRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        work.setId(id);
        Work updatedWork = workRepository.save(work);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedWork);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWork(@PathVariable Long id) {
        if (!workRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        workRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteWorks(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                workRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
            }
        }
        return ResponseEntity.noContent().build();
    }
}