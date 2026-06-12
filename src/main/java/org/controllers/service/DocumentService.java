package org.controllers.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controllers.model.dto.DocumentResponse;
import org.controllers.model.entity.DocumentEntity;
import org.controllers.repository.jpa.DocumentRepository;
import org.controllers.service.parse.DocumentParse;
import org.controllers.service.search.IndexingService;
import org.controllers.service.storage.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final FileStorageService fileStorageService;
    private final DocumentRepository documentRepository;
    private final IndexingService indexingService;
    private final List<DocumentParse> parsers;

    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        log.info("Processing document upload: {}", originalFilename);

        String filePath = fileStorageService.storeFile(file);
        String extension = fileStorageService.getFileExtension(originalFilename);
        log.info("File stored at: {}, extension: {}", filePath, extension);

        DocumentEntity document = DocumentEntity.builder()
                .originalFilename(originalFilename)
                .storedFilename(filePath.substring(filePath.lastIndexOf("/") + 1))
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .fileExtension(extension)
                .fileSize(file.getSize())
                .filePath(filePath)
                .uploadedAt(LocalDateTime.now())
                .indexed(false)  // Явно указываем что не проиндексирован
                .build();

        document = documentRepository.save(document);
        log.info("Document metadata saved with ID: {}", document.getId());

        String content = null;
        try {
            content = extractText(filePath, extension);
            log.info("Text extracted, length: {}", content != null ? content.length() : 0);
        } catch (Exception e) {
            log.error("Failed to extract text from {}: {}", originalFilename, e.getMessage());
            return mapToResponse(document, "Document saved but text extraction failed: " + e.getMessage());
        }

        if (content == null || content.trim().isEmpty()) {
            log.warn("Extracted text is empty for: {}", originalFilename);
            return mapToResponse(document, "Document saved but text is empty");
        }

        try {
            String esId = indexingService.indexDocument(document, content);
            log.info("Document indexed in ES with ID: {}", esId);

            document.setIndexed(true);
            document.setIndexedAt(LocalDateTime.now());
            documentRepository.save(document);

            return mapToResponse(document, "Document uploaded and indexed successfully");

        } catch (Exception e) {
            log.error("Failed to index document {}: {}", originalFilename, e.getMessage(), e);
            return mapToResponse(document, "Document saved but indexing failed: " + e.getMessage());
        }
    }

    private String extractText(String filePath, String extension) throws Exception {
        log.info("Looking for parser for extension: {}", extension);

        DocumentParse parser = parsers.stream()
                .filter(p -> p.supports(extension))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException(
                        "No parser found for extension: " + extension));

        log.info("Using parser: {} for file: {}", parser.getClass().getSimpleName(), filePath);

        try (InputStream inputStream = fileStorageService.getFileContent(filePath)) {
            String text = parser.parse(inputStream);
            log.info("Parser returned text of length: {}", text != null ? text.length() : 0);
            return text;
        } catch (Exception e) {
            log.error("Error reading file or parsing: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll().stream()
                .map(doc -> mapToResponse(doc, null))
                .collect(Collectors.toList());
    }

    public DocumentResponse getDocument(Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));
        return mapToResponse(document, null);
    }

    @Transactional
    public void deleteDocument(Long id) {
        DocumentEntity document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found: " + id));

        try {
            indexingService.deleteDocument(id);
        } catch (Exception e) {
            log.error("Error deleting document from ES: {}", e.getMessage());
        }

        fileStorageService.deleteFile(document.getFilePath());

        documentRepository.delete(document);

        log.info("Document deleted: {}", document.getOriginalFilename());
    }

    private DocumentResponse mapToResponse(DocumentEntity document) {
        return mapToResponse(document, null);
    }

    private DocumentResponse mapToResponse(DocumentEntity document, String message) {
        // Определяем сообщение на основе статуса
        String finalMessage = message;
        if (finalMessage == null) {
            finalMessage = document.isIndexed() ? "Indexed" : "Not indexed";
        }

        return DocumentResponse.builder()
                .id(document.getId())
                .originalFilename(document.getOriginalFilename())
                .contentType(document.getContentType())
                .fileSize(document.getFileSize())
                .fileExtension(document.getFileExtension())
                .filePath(document.getFilePath())  // Добавь это поле в DocumentResponse!
                .indexed(document.isIndexed())
                .uploadedAt(document.getUploadedAt())
                .message(finalMessage)
                .build();
    }
}