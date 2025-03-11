package com.example.demo.service;

import com.example.demo.entity.AttachmentDTO;
import com.example.demo.entity.CollectionDTO;
import com.example.demo.repository.AttachmentRepository;
import com.example.demo.repository.CollectionRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Service
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final GoogleClassroomService googleClassroomService;
    private final AttachmentRepository attachmentRepository;
    private final BackgroundService backgroundService;

    @Autowired
    public CollectionService(CollectionRepository collectionRepository,
                             GoogleClassroomService googleClassroomService,
                             AttachmentRepository attachmentRepository,
                             BackgroundService backgroundService) {
        this.collectionRepository = collectionRepository;
        this.googleClassroomService = googleClassroomService;
        this.attachmentRepository = attachmentRepository;
        this.backgroundService = backgroundService;
    }

    public List<CollectionDTO> getCollections() {
        return collectionRepository.findAll();
    }

    public CollectionDTO getCollection(String collectionId) {
        return collectionRepository.findById(collectionId).orElseThrow();
    }

    public List<AttachmentDTO> getAttachments(String collectionId) {
        CollectionDTO collection = collectionRepository.findById(collectionId).orElseThrow();
        return attachmentRepository.findByCollection(collection);
    }

    @Transactional
    public void addCollection(String accessToken, String courseId, String courseWorkId) throws GeneralSecurityException, IOException {
        String courseName = googleClassroomService.getCourse(accessToken, courseId).getName();
        CollectionDTO collection = new CollectionDTO(courseId, courseName, courseWorkId, true);
        collectionRepository.save(collection);
        attachmentRepository.deleteByCollection(collection);
        backgroundService.checkAttachments(accessToken, collection);
    }
}
