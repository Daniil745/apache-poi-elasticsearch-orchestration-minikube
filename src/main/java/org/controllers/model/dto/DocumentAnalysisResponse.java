package org.controllers.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentAnalysisResponse {
    private String answer;
    private boolean success;
    private String errorMessage;

    private DocumentAnalysisResponse() {}

    public static DocumentAnalysisResponse success(String answer) {
        DocumentAnalysisResponse response = new DocumentAnalysisResponse();
        response.answer = answer;
        response.success = true;
        return response;
    }

    public static DocumentAnalysisResponse error(String errorMessage) {
        DocumentAnalysisResponse response = new DocumentAnalysisResponse();
        response.errorMessage = errorMessage;
        response.success = false;
        return response;
    }
}
