package org.controllers.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controllers.model.dto.DocumentAnalysisRequest;
import org.controllers.model.dto.DocumentAnalysisResponse;
import org.controllers.model.dto.DocumentResponse;
import org.controllers.service.DocumentService;
import org.controllers.service.documentAnalysis.DocumentAIAnalysis;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class DocumentController {

    private final DocumentService documentService;
    private final DocumentAIAnalysis documentAIAnalysis;

    @PostMapping("/upload")
    public ResponseEntity<DocumentResponse> uploadDocument(@RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            DocumentResponse documentResponse = documentService.uploadDocument(file);
            return ResponseEntity.status(HttpStatus.CREATED).body(documentResponse);
        } catch (Exception e) {

            log.error("Error uploading file: {}", file.getOriginalFilename(), e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(DocumentResponse.builder()
                            .originalFilename(file.getOriginalFilename())
                            .message("Error uploading file: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<DocumentAnalysisResponse> analyzeDocument(@RequestBody DocumentAnalysisRequest request) {
        try {
            String userQuestion = request.getQuestion();

            if (userQuestion == null || userQuestion.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(DocumentAnalysisResponse.error("Question cannot be empty"));
            }

            String analysisResult = documentAIAnalysis.searchAnalysisDoc(userQuestion);

            return ResponseEntity.ok(DocumentAnalysisResponse.success(analysisResult));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(DocumentAnalysisResponse.error("Error processing request: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<DocumentResponse>> getAllDocuments() {
        List<DocumentResponse> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocumentResponse> getDocument(@PathVariable Long id) {
        try {
            DocumentResponse document = documentService.getDocument(id);
            return ResponseEntity.ok(document);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        try {
            documentService.deleteDocument(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
