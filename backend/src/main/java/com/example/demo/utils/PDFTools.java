package com.example.demo.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PDFTools {

    public static String extractFirstPageText(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream.readAllBytes()))) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(1);
            pdfStripper.setEndPage(1);
            return pdfStripper.getText(document);
//            return pdfStripper.getText(document).toLowerCase()
//                    .replaceAll("\n", " ")
//                    .replaceAll("\\s+", " ").trim();
        }
    }

    public static boolean isNameMentioned(String fullName, String pageText) {
        if (fullName == null || pageText == null) return false;

        List<String> variants = getVariants(fullName);

        String normalizedText = pageText.toLowerCase();

        return variants.stream().anyMatch(variant ->
                normalizedText.contains(variant.toLowerCase())
        );
    }

    public static String extractSurnameInitials(String input) {
        String cleaned = input.trim().replaceAll("\\s+", " ");
        Pattern pattern = Pattern.compile("([А-ЯІЇЄҐ][а-яіїєґ']+\\s[А-ЯІЇЄҐ]\\.\\s?[А-ЯІЇЄҐ]?\\.)");

        Matcher matcher = pattern.matcher(cleaned);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    public static List<String> getVariants(String fullName) {
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length < 2) return new ArrayList<>();

        String lastName = parts[0];
        String firstName = parts[1];
        String middleName = parts.length > 2 ? parts[2] : "";

        List<String> variants = new ArrayList<>();

        variants.add(lastName + " " + firstName);
        variants.add(lastName + " " + firstName.charAt(0) + ".");
        if (!middleName.isEmpty()) {
            variants.add(lastName + " " + firstName.charAt(0) + ". " + middleName.charAt(0) + ".");
        }
        variants.add(fullName);

        return variants;
    }

    public static boolean fuzzyMatchFullName(String fullName, String titleText) throws IOException {
        Directory memoryIndex = new ByteBuffersDirectory();
        Analyzer analyzer = new StandardAnalyzer();

        try (IndexWriter writer = new IndexWriter(memoryIndex, new IndexWriterConfig(analyzer))) {
            Document document = new Document();
            document.add(new TextField("content", titleText, Field.Store.YES));
            writer.addDocument(document);
        }

        List<String> variants = getVariants(fullName);

        try (IndexReader reader = DirectoryReader.open(memoryIndex)) {
            IndexSearcher searcher = new IndexSearcher(reader);

            for (String variant : variants) {
                String[] tokens = variant.toLowerCase().split("\\s+");

                BooleanQuery.Builder builder = new BooleanQuery.Builder();
                for (String token : tokens) {
                    builder.add(new FuzzyQuery(new Term("content", token), 2), BooleanClause.Occur.MUST);
                }

                Query query = builder.build();
                TopDocs hits = searcher.search(query, 1);

                if (hits.totalHits.value() > 0) return true;
            }
        }

        return false;
    }

    public static String normalizeTitleText(String rawText) {
        return rawText
                .toLowerCase(Locale.ROOT)
                .replaceAll("[_\\-\\.]{2,}", " ")
                // лапки?
                .replaceAll("[^а-яґєіїa-z0-9.«»’‘'\"\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }


    public static byte[] removeAppendices(InputStream inputStream) throws IOException {
        try (PDDocument document = Loader.loadPDF(new RandomAccessReadBuffer(inputStream.readAllBytes()))) {
            final String appendixRegex = "^(ДОДАТОК\\s+[A-ZА-ЯІЇЄ№0-9]+|ДОДАТКИ|APPENDIX(ES)?\\s+[A-Z0-9]*)";
            final Pattern appendixPattern = Pattern.compile(appendixRegex, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE | Pattern.MULTILINE);
            int pageCount = document.getNumberOfPages();
            int appendixPage = -1;
            PDFTextStripper pdfStripper = new PDFTextStripper();

            for (int pIdx = 1; pIdx < pageCount; pIdx++) {
                pdfStripper.setStartPage(pIdx);
                pdfStripper.setEndPage(pIdx);
                String text = pdfStripper.getText(document).trim();
                Matcher matcher = appendixPattern.matcher(text);
                if (matcher.find()) {
                    appendixPage = pIdx;
                    break;
                }
            }

            for (int i = pageCount - 1; i >= appendixPage && i >= 0; i--) {
                document.removePage(i);
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            document.save(output);
            document.close();
            return output.toByteArray();
        }
    }
}
