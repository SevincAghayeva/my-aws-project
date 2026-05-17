package com.pm.myawsproject.controller;

import com.pm.myawsproject.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            String docId = documentService.handleDocumentUpload(file.getOriginalFilename(), file.getBytes());
            return ResponseEntity.ok(
                    "File uploaded successfully! Document ID: " + docId
            );
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while reading the file: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, String>> getMetadata(@PathVariable("id") String id) {
        Map<String, String> metadata = documentService.getDocumentMetadata(id);
        if (metadata.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(metadata);
    }
}