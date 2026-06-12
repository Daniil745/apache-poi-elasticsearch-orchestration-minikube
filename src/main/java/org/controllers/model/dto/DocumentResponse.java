package org.controllers.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentResponse {
    private Long id;

    private String originalFilename;

    private String contentType;

    private String fileExtension;

    private Long fileSize;

    private String filePath;

    private boolean indexed;

    private LocalDateTime uploadedAt;

    private String message;
}
