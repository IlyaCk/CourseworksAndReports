package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @GetMapping("/")
    public List<Notification> getNotifications(
            @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) {
        User currentUser = userRepository.findByEmail(authorizedClient.getPrincipalName()).orElseThrow();
        return notificationRepository.findByRecipientOrderByCreatedAtDesc(currentUser);
    }

    @PostMapping("/{id}/mark-as-read")
    public void markAsRead(@PathVariable Long id,
                           @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient
    ) {
        notificationRepository.findById(id).ifPresent(notification -> {
            if (Objects.equals(notification.getRecipient().getEmail(), authorizedClient.getPrincipalName())) {
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    @PostMapping("/mark-all-as-read")
    public void markAllAsRead(@RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) {
        notificationRepository.findByRecipientEmail(authorizedClient.getPrincipalName()).forEach(notification -> {
            if (!notification.isRead()){
                notification.setRead(true);
                notificationRepository.save(notification);
            }
        });
    }
}
