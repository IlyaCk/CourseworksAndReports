package com.example.demo.controller.data;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
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
@RequestMapping("/api/data/users")
public class UserDataController {

    @Autowired
    private UserRepository userRepository;

    // GET /api/users
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
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

        // Обробка поля сортування - розбиваємо на окремі поля, якщо є кома
        String[] sortFields = sort.split(",");
        String actualSortField = sortFields[0]; // Беремо перше поле

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, actualSortField));

        Page<User> userPage = userRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", userPage.getContent());
        response.put("total", userPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getUser(@PathVariable Long id) {
        Optional<User> userOpt = userRepository.findById(id);

        if (userOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", userOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/users
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        User createdUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // PUT /api/users/{id}
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        user.setId(id);
        User updatedUser = userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedUser);
        return ResponseEntity.ok(response);
    }

    // DELETE /api/users/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE Many /api/users
    @DeleteMapping
    public ResponseEntity<Void> deleteUsers(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                userRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
                // Ігноруємо помилки видалення неіснуючих записів
            }
        }
        return ResponseEntity.noContent().build();
    }
}