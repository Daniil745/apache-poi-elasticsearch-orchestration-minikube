package org.controllers.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @NotBlank
    @Size(min = 2, max = 500)
    private String query;

    private String fileType;

    private Integer page;

    private Integer size;
}
