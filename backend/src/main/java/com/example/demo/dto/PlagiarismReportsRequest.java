package com.example.demo.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class PlagiarismReportsRequest {
    List<MultipartFile> files;
}
