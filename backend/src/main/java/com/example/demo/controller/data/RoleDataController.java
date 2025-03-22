package com.example.demo.controller.data;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/data/roles")
public class RoleDataController {

    @Autowired
    private RoleRepository roleRepository;

    // GET /api/roles
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllRoles(
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
                // Просто ігноруємо помилки парсингу
            }
        }

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, sort));

        Page<Role> rolePage = roleRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", rolePage.getContent());
        response.put("total", rolePage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // GET /api/roles/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getRole(@PathVariable Long id) {
        Optional<Role> roleOpt = roleRepository.findById(id);

        if (roleOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", roleOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/roles
    @PostMapping
    public ResponseEntity<Map<String, Object>> createRole(@RequestBody Role role) {
        Role createdRole = roleRepository.save(role);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/roles/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRole(@PathVariable Long id, @RequestBody Role role) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        role.setId(id);
        Role updatedRole = roleRepository.save(role);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedRole);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/roles/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        if (!roleRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        roleRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE Many /api/roles
    @DeleteMapping
    public ResponseEntity<Void> deleteRoles(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                roleRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
                // Ігноруємо помилки видалення неіснуючих записів
            }
        }
        return ResponseEntity.noContent().build();
    }
}