package com.example.demo.utils;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.io.InputStream;

public class PDFTools {

    public static boolean checkPdfForKeyword(InputStream inputStream, String keyword) throws IOException {
        RandomAccessReadBuffer memoryBuffer = new RandomAccessReadBuffer(inputStream.readAllBytes());
        PDDocument document = Loader.loadPDF(memoryBuffer);
        PDFTextStripper pdfStripper = new PDFTextStripper();
        pdfStripper.setStartPage(1);
        pdfStripper.setEndPage(1);
        String firstPageText = pdfStripper.getText(document);
        document.close();
        return firstPageText.toLowerCase().contains(keyword.toLowerCase());
    }
}
