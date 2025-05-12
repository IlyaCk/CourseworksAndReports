package com.example.demo.controller.data;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
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
@RequestMapping("/api/data/notifications")
public class NotificationDataController {

    private final NotificationRepository notificationRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllNotifications(
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

        Sort.Direction direction = order.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, perPage, Sort.by(direction, sort));

        Page<Notification> notificationPage = notificationRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("data", notificationPage.getContent());
        response.put("total", notificationPage.getTotalElements());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getNotification(@PathVariable Long id) {
        Optional<Notification> notificationOpt = notificationRepository.findById(id);

        if (notificationOpt.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("data", notificationOpt.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createNotification(@RequestBody Notification notification) {
        Notification createdNotification = notificationRepository.save(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("data", createdNotification);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateNotification(@PathVariable Long id, @RequestBody Notification notification) {
        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        notification.setId(id);
        Notification updatedNotification = notificationRepository.save(notification);

        Map<String, Object> response = new HashMap<>();
        response.put("data", updatedNotification);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable Long id) {
        if (!notificationRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        notificationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteNotifications(@RequestParam String ids) {
        String[] idArray = ids.split(",");
        for (String id : idArray) {
            try {
                notificationRepository.deleteById(Long.parseLong(id));
            } catch (Exception e) {
            }
        }
        return ResponseEntity.noContent().build();
    }
}