package com.example.demo.controller.data;

import com.example.demo.entity.Discipline;
import com.example.demo.repository.DisciplineRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/data/disciplines")
public class DisciplineDataController {

    @Autowired
    private DisciplineRepository disciplineRepository;

    // GET /api/data/disciplines
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllDisciplines(
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
                // Ігноруємо помилки парсингу
            }
        }

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, sort));

        Page<Discipline> disciplinePage = disciplineRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", disciplinePage.getContent());
        response.put("total", disciplinePage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // GET /api/data/disciplines/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getDiscipline(@PathVariable Long id) {
        Optional<Discipline> disciplineOpt = disciplineRepository.findById(id);

        if (disciplineOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", disciplineOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/data/disciplines
    @PostMapping
    public ResponseEntity<Map<String, Object>> createDiscipline(@RequestBody Discipline discipline) {
        Discipline createdDiscipline = disciplineRepository.save(discipline);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdDiscipline);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/data/disciplines/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateDiscipline(@PathVariable Long id, @RequestBody Discipline discipline) {
        if (!disciplineRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        discipline.setId(id);
        Discipline updatedDiscipline = disciplineRepository.save(discipline);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedDiscipline);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/data/disciplines/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDiscipline(@PathVariable Long id) {
        if (!disciplineRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        disciplineRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE Many /api/data/disciplines
    @DeleteMapping
    public ResponseEntity<Void> deleteDisciplines(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                disciplineRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
                // Ігноруємо помилки видалення неіснуючих записів
            }
        }
        return ResponseEntity.noContent().build();
    }
}