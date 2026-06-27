package org.controllers.service.documentAnalysis;

import org.controllers.model.elastic.DocumentIndex;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DocumentAIAnalysis {
    private final ElasticsearchOperations elasticsearchOperations;
    private final ChatClient chatClient;

    public DocumentAIAnalysis(ElasticsearchOperations elasticsearchOperations, ChatClient.Builder chatClientBuilder) {
        this.elasticsearchOperations = elasticsearchOperations;
        this.chatClient = chatClientBuilder.build();
    }

    public String searchAnalysisDoc(String userQuestion) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.matchAll(m -> m))
                .withMaxResults(5)
                .build();

        SearchHits<DocumentIndex> documentIndexSearchHits = elasticsearchOperations.search(query, DocumentIndex.class);

        List<DocumentIndex> foundObjects = documentIndexSearchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        if (foundObjects.isEmpty()) {
            return "Документы не найдены в базе";
        }

        String aiContext = foundObjects.stream()
                .map(doc -> String.format("Файл: %s\nСодержание:\n%s", doc.getFilename(), doc.getContent()))
                .collect(Collectors.joining("\n\n----\n\n"));

        return this.chatClient.prompt()
                .system(s -> s.text("Ты - ассистент по анализу документов. Отвечай на русском языке кратко и по делу."))
                .user(u -> u.text("Документы:\n{context}\n\nВопрос: {question}")
                        .param("context", aiContext)
                        .param("question", userQuestion))
                .call()
                .content();
    }
}