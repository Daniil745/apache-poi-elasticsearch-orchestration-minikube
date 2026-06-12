package org.controllers.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.controllers.model.dto.SearchRequest;
import org.controllers.model.dto.SearchResponse;
import org.controllers.service.search.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SearchController {

    private final SearchService searchService;

    @PostMapping
    public ResponseEntity<SearchResponse> search(@Valid @RequestParam SearchRequest searchRequest) {
        SearchResponse searchResponse = searchService.search(searchRequest);
        return ResponseEntity.ok(searchResponse);
    }

    @GetMapping
    public ResponseEntity<SearchResponse> quickSearch(
            @RequestParam("q") String query,
            @RequestParam(value = "type", required = false) String fileType) {

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .fileType(fileType)
                .build();

        SearchResponse response = searchService.search(searchRequest);
        return ResponseEntity.ok(response);
    }

}
