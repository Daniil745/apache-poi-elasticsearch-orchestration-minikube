package org.controllers.model.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private String query;

    private long totalHits;

    private List<SearchResult> results;

    private int page;

    private int size;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchResult {
        private Long documentId;

        private String filename;

        private String fileType;

        private Long fileSize;

        private LocalDateTime uploadedAt;

        private List<String> highLights;

        private float score;
    }
}
