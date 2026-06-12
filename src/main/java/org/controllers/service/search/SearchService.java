package org.controllers.service.search;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.controllers.config.ElasticsearchProperties;
import org.controllers.model.dto.SearchRequest;
import org.controllers.model.dto.SearchResponse;
import org.controllers.model.elastic.DocumentIndex;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {


    private final ElasticsearchOperations elasticsearchOperations;
    private final ElasticsearchProperties esProperties;

    public SearchResponse search(SearchRequest searchRequest) {
        log.info("Searching for: {}", searchRequest.getQuery());

        NativeQuery nativeQuery = buildNativeQuery(searchRequest);

        SearchHits<DocumentIndex> searchHits =
                elasticsearchOperations.search(nativeQuery, DocumentIndex.class);

        return mapSearchResults(searchHits, searchRequest);
    }

    private NativeQuery buildNativeQuery(SearchRequest searchRequest) {

        Query searchQuery = MatchQuery.of(m -> m
                .field("content")
                .query(searchRequest.getQuery())
                .fuzziness("AUTO")
        )._toQuery();

        if (searchRequest.getFileType() != null && !searchRequest.getFileType().isEmpty()) {
            Query filterQuery = MatchQuery.of(m -> m
                    .field("fileType")
                    .query(searchRequest.getFileType())
            )._toQuery();

            Query finalSearchQuery = searchQuery;
            searchQuery = BoolQuery.of(b -> b
                    .must(finalSearchQuery)
                    .filter(filterQuery)
            )._toQuery();
        }

        HighlightField highlightField = new HighlightField("content");

        Highlight highlight = new Highlight(List.of(highlightField));
        HighlightQuery highlightQuery = new HighlightQuery(highlight, DocumentIndex.class);

        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null
                ? Math.min(searchRequest.getSize(), esProperties.getSearch().getMaxSize())
                : esProperties.getSearch().getDefaultSize();

        return NativeQuery.builder()
                .withQuery(searchQuery)
                .withPageable(PageRequest.of(page, size))
                .withHighlightQuery(highlightQuery)
                .build();
    }

    private SearchResponse mapSearchResults(SearchHits<DocumentIndex> searchHits,
                                            SearchRequest request) {
        List<SearchResponse.SearchResult> results = searchHits.getSearchHits().stream()
                .map(this::mapToSearchResult)
                .collect(Collectors.toList());

        return SearchResponse.builder()
                .query(request.getQuery())
                .totalHits(searchHits.getTotalHits())
                .results(results)
                .page(request.getPage() != null ? request.getPage() : 0)
                .size(results.size())
                .build();
    }

    private SearchResponse.SearchResult mapToSearchResult(SearchHit<DocumentIndex> hit) {
        DocumentIndex document = hit.getContent();

        List<String> highlights = hit.getHighlightField("content");

        return SearchResponse.SearchResult.builder()
                .documentId(document.getDocumentId())
                .filename(document.getFilename())
                .fileType(document.getFileType())
                .fileSize(document.getFileSize())
                .uploadedAt(document.getUploadedAt())
                .highLights(highlights)
                .score(hit.getScore())
                .build();
    }

}
