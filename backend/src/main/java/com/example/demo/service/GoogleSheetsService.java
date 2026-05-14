package com.example.demo.service;

import com.example.demo.utils.PDFTools;
import com.example.demo.utils.StrDist;
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

//    public String extractGidFromUrl(String url) {
//        return url.contains("#gid=") ? url.split("#gid=")[1] : "0";
//    }

    public List<AssignmentRecord> extractAssignments(String accessToken, String link)
            throws IOException, GeneralSecurityException {

        Sheets sheetsService = getSheetsService(accessToken);

        String spreadsheetId = GoogleDriveService.extractFileIdFromLink(link);
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
            String group = columnMap.containsKey("Група") ? getCell(row, columnMap.get("Група")) : "";
            String reviewer = columnMap.containsKey("Рецензент") ? getCell(row, columnMap.get("Рецензент")) : "";
            supervisor = PDFTools.extractSurnameInitials(supervisor);

            System.out.println("i = " + i);
            System.out.println("student = " + student);
            System.out.println("topic = " + topic);
            System.out.println("group = " + group);
            System.out.println("supervisor = " + supervisor);
            System.out.println("reviewer = " + reviewer);

            // Пропускаємо пусті рядки або неповні записи
            if (student.isBlank() || topic.isBlank() || supervisor.isBlank()) continue;

            results.add(new AssignmentRecord(student, topic, supervisor, group, reviewer));
        }

        return results;
    }

    private String getCell(List<Object> row, int index) {
        if (index >= row.size()) return "";
        return row.get(index).toString().trim();
    }

    final static String headerStudent = "ПІБ студента";
    final static String headerSupervisor = "Керівник роботи";
    final static String headerGroup = "Група";
    final static String headerTheme = "Тема роботи";
    final static String headerReviewer = "Рецензент";

    record HeaderName (String searchPatt, String keyName, boolean mandatory) {}

    static final List<HeaderName> headerNames = List.of(
            new HeaderName("тема", headerTheme, true),
            new HeaderName("студент", headerStudent, false),
            new HeaderName("виконавець", headerStudent, false),
            new HeaderName("група", headerGroup, false),
            new HeaderName("керівник", headerSupervisor, false),
            new HeaderName("викладач", headerSupervisor, false),
            new HeaderName("рецензент", headerReviewer, false)
    );


    private Map<String, Integer> mapColumns(List<List<Object>> rows) {
        Map<String, Integer> columnMap = new HashMap<>();
        Set<Integer> alreadyUsed = new HashSet<>();

        for (int i = 0; i < rows.size(); i++) {
            List<Object> headers = rows.get(i);
            for (int k = 0; k < headerNames.size(); k++) {
                int bestDist = Integer.MAX_VALUE / 2;
                int bestJ = -1;
                String search = headerNames.get(k).searchPatt();
                for (int j = 0; j < headers.size(); j++) {
                    if (alreadyUsed.contains(j))
                        continue;
                    String header = headers.get(j).toString().toLowerCase();
                    StrDist.DistResInfo dist = StrDist.calcStrDist(search, header,
                            StrDist.SearchBorder.WORD, StrDist.SearchBorder.WORD, false, true);
                    if (dist.matchLevel.betterOrEqual(StrDist.MatchLevel.MEDIUM) && dist.dist < bestDist) {
                        bestDist = dist.dist;
                        bestJ = j;
                    }
                }
                if(bestJ == -1) {
                    if (headerNames.get(k).mandatory()) {
                        break;
                    }
                } else {
                    columnMap.put(headerNames.get(k).keyName(), bestJ);
                    alreadyUsed.add(bestJ);
                }
            }
            if (columnMap.size() >= 3 && columnMap.containsKey(headerSupervisor) && columnMap.containsKey(headerStudent) && columnMap.containsKey(headerTheme)) {
                columnMap.put("Рядок заголовку", i);
                break;
            }
            columnMap.clear();
            alreadyUsed.clear();
        }
        return columnMap;
    }

    public record AssignmentRecord(String student, String topic, String supervisor, String group, String reviewer) {
    }
}
