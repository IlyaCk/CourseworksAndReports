package com.example.demo.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "coursework-management";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    public Drive getGoogleDriveService(String accessToken) throws GeneralSecurityException, IOException {
        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken)
        )
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

    public InputStream getFileContent(String accessToken, String link) throws GeneralSecurityException, IOException {
        Drive driveService = getGoogleDriveService(accessToken);
        return driveService.files().get(extractIdFromLink(link)).executeMediaAsInputStream();
    }

    public String extractIdFromLink(String url) {
        String[] parts = url.split("/d/")[1].split("/");
        return parts[0];
    }
}
