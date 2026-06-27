/**
 * Request DTO for AI-powered document analysis endpoint.
 * Contains user's natural language question about indexed documents.
 */

package org.controllers.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DocumentAnalysisRequest {
    private String question;
}
