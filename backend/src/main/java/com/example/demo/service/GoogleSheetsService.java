package com.example.demo.service;

import com.example.demo.utils.PDFTools;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

@Service
public class GoogleSheetsService {
    private static final String APPLICATION_NAME = "coursework-management";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    private Sheets getSheetsService(String accessToken) throws GeneralSecurityException, IOException {
        return new Sheets.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JSON_FACTORY,
                request -> request.getHeaders().setAuthorization("Bearer " + accessToken))
                .setApplicationName(APPLICATION_NAME)
                .build();
    }

//    public String getSheetNameByGid(String accessToken, String spreadsheetId, String gid)
//            throws IOException, GeneralSecurityException {
//
//        Sheets sheetsService = getSheetsService(accessToken);
//        Spreadsheet spreadsheet = sheetsService.spreadsheets().get(spreadsheetId).execute();
//
//        for (Sheet sheet : spreadsheet.getSheets()) {
//            SheetProperties props = sheet.getProperties();
//            if (String.valueOf(props.getSheetId()).equals(gid)) {
//                return props.getTitle();
//            }
//        }
//
//        return spreadsheet.getSheets().getFirst().getProperties().getTitle();
//    }

    public String extractSpreadsheetIdFromUrl(String url) {
        String[] parts = url.split("/d/")[1].split("/");
        return parts[0];
    }

//    public String extractGidFromUrl(String url) {
//        return url.contains("#gid=") ? url.split("#gid=")[1] : "0";
//    }

    public List<AssignmentRecord> extractAssignments(String accessToken, String link)
            throws IOException, GeneralSecurityException {

        Sheets sheetsService = getSheetsService(accessToken);

        String spreadsheetId = extractSpreadsheetIdFromUrl(link);
//        Береться gid з URL, коли закоментовано береться перший листок
//        String gid = extractGidFromUrl(link);
//        String sheetName = getSheetNameByGid(accessToken, spreadsheetId, gid);
//        String range = sheetName + "!A1:Z";
        ValueRange response = sheetsService.spreadsheets().values()
                .get(spreadsheetId, "!A1:Z")
                .execute();

        List<List<Object>> rows = response.getValues();
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, Integer> columnMap = mapColumns(rows);

        List<AssignmentRecord> results = new ArrayList<>();

        for (int i = columnMap.get("Рядок заголовку") + 1; i < rows.size(); i++) {
            List<Object> row = rows.get(i);
            if (row.size() < columnMap.size()) continue;

            String student = getCell(row, columnMap.get("ПІБ студента"));
            String topic = getCell(row, columnMap.get("Тема роботи"));
            String supervisor = getCell(row, columnMap.get("Керівник роботи"));
            String group = getCell(row, columnMap.get("Група"));
            supervisor = PDFTools.extractSurnameInitials(supervisor);

            // Пропускаємо пусті рядки або неповні записи
            if (student.isBlank() || topic.isBlank() || supervisor.isBlank()) continue;

            results.add(new AssignmentRecord(student, topic, supervisor, group));
        }

        return results;
    }

    private String getCell(List<Object> row, int index) {
        if (index >= row.size()) return "";
        return row.get(index).toString().trim();
    }

    private Map<String, Integer> mapColumns(List<List<Object>> rows) {
        Map<String, Integer> columnMap = new HashMap<>();

        for (int i = 0; i < rows.size(); i++) {
            List<Object> headers = rows.get(i);
            for (int j = 0; j < headers.size(); j++) {
                String header = headers.get(j).toString().toLowerCase();
                if (header.contains("студент")) columnMap.put("ПІБ студента", j);
                else if (header.contains("тема")) columnMap.put("Тема роботи", j);
                else if (header.contains("керівник")) columnMap.put("Керівник роботи", j);
                else if (header.contains("група")) columnMap.put("Група", j);
            }
            if (columnMap.size() == 4) {
                columnMap.put("Рядок заголовку", i);
                break;
            }
        }

        return columnMap;
    }

    public record AssignmentRecord(String student, String topic, String supervisor, String group) {
    }
}
