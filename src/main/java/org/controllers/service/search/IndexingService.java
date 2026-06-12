package org.controllers.service.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controllers.model.elastic.DocumentIndex;
import org.controllers.model.entity.DocumentEntity;
import org.controllers.repository.elasticsearch.DocumentSearchRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexingService {

    private final DocumentSearchRepository documentSearchRepository;

    public String indexDocument(DocumentEntity document, String content) {
        log.info("Indexing document: {}", document.getOriginalFilename());

        DocumentIndex indexDoc = DocumentIndex.builder()
                .id(String.valueOf(document.getId()))  // Используем ID из БД
                .documentId(document.getId())
                .content(content)
                .filename(document.getOriginalFilename())
                .fileType(getFileType(document.getFileExtension()))
                .fileExtension(document.getFileExtension())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .filePath(document.getFilePath())
                .build();

        DocumentIndex saved = documentSearchRepository.save(indexDoc);

        log.info("Document indexed successfully: {}", document.getOriginalFilename());
        return saved.getId();
    }

    public void updateDocument(DocumentEntity document, String newContent) {
        documentSearchRepository.deleteById(String.valueOf(document.getId()));

        indexDocument(document, newContent);
    }

    public void deleteDocument(Long id) {
        documentSearchRepository.deleteById(String.valueOf(id));
    }

    private String getFileType(String extension) {
        return switch (extension.toLowerCase()) {
            case "xlsx", "xls" -> "excel";
            case "docx", "doc" -> "word";
            case "pptx", "ppt" -> "powerpoint";
            default -> "unknown";
        };
    }

}
