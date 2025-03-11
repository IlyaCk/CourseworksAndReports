package com.example.demo.service;

import com.example.demo.entity.AttachmentDTO;
import com.example.demo.entity.CollectionDTO;
import com.example.demo.repository.AttachmentRepository;
import com.example.demo.repository.CollectionRepository;
import com.example.demo.utils.PDFTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class BackgroundService {
    private final CollectionRepository collectionRepository;
    private final GoogleClassroomService googleClassroomService;
    private final AttachmentRepository attachmentRepository;
    private final GoogleDriveService googleDriveService;

    @Autowired
    public BackgroundService(CollectionRepository collectionRepository,
                             GoogleClassroomService googleClassroomService,
                             AttachmentRepository attachmentRepository,
                             GoogleDriveService googleDriveService) {
        this.collectionRepository = collectionRepository;
        this.googleClassroomService = googleClassroomService;
        this.attachmentRepository = attachmentRepository;
        this.googleDriveService = googleDriveService;
    }

    @Async("asyncExecutor")
    public void checkAttachments(String accessToken, CollectionDTO collection) throws GeneralSecurityException, IOException {
        List<AttachmentDTO> attachments = googleClassroomService.getAttachmentsFromSubmissions(accessToken, collection.getId(), collection.getTaskId(), collection);
        for (AttachmentDTO attachment : attachments) {
            boolean containsKeyword = PDFTools.checkPdfForKeyword(googleDriveService.getFileContent(accessToken, attachment.getId()), "Курсова робота");
            attachment.setIsCoursework(containsKeyword);
            attachmentRepository.save(attachment);
        }
        collection.setIsUpdating(false);
        collection.setUpdatedAt(LocalDateTime.now());
        collectionRepository.save(collection);
        // attachmentRepository.saveAll(attachments);
    }
}
