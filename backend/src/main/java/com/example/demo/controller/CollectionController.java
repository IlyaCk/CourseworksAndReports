package com.example.demo.controller;

import com.example.demo.entity.AttachmentDTO;
import com.example.demo.entity.CollectionDTO;
import com.example.demo.service.CollectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {
    private final CollectionService collectionService;

    @Autowired
    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/")
    public List<CollectionDTO> getCollections() {
        return collectionService.getCollections();
    }

    @GetMapping("/{collectionId}")
    public CollectionDTO getCollection(@PathVariable String collectionId) {
        return collectionService.getCollection(collectionId);
    }

    @GetMapping("/{collectionId}/attachments")
    public List<AttachmentDTO> getAttachments(@PathVariable String collectionId) {
        return collectionService.getAttachments(collectionId);
    }

    @PostMapping("/add")
    public ResponseEntity<Object> addCollection(@RequestBody Map<String, String> payload,
                                        @RegisteredOAuth2AuthorizedClient("google") OAuth2AuthorizedClient authorizedClient) throws GeneralSecurityException, IOException {
        String courseId = payload.get("courseId");
        String courseWorkId = payload.get("courseWorkId");
        OAuth2AccessToken accessToken = authorizedClient.getAccessToken();
        collectionService.addCollection(accessToken.getTokenValue(), courseId, courseWorkId);
        return ResponseEntity.ok().build();
    }
}
