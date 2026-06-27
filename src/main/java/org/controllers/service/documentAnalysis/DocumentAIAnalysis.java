/**
 * AI-powered document analysis service that combines Elasticsearch retrieval
 * with Ollama LLM for intelligent question answering over indexed documents.

 * Flow:
 * 1. Retrieves top 5 documents from Elasticsearch using matchAll query
 * 2. Builds context from document filenames and content
 * 3. Sends context + user question to Ollama LLM via Spring AI ChatClient
 * 4. Returns AI-generated answer based solely on provided document context
  
 * The service uses a retrieval-augmented generation (RAG) pattern:
 * - Retrieval: Elasticsearch fetches relevant documents
 * - Augmentation: Documents are formatted into LLM context
 * - Generation: Ollama generates answer from provided context
 */

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
            return "Document not found in base";
        }

        String aiContext = foundObjects.stream()
                .map(doc -> String.format("File: %s\nContain:\n%s", doc.getFilename(), doc.getContent()))
                .collect(Collectors.joining("\n\n----\n\n"));

        return this.chatClient.prompt()
                .system(s -> s.text("You are a document analysis assistant. Answer in English, briefly and to the point."))
                .user(u -> u.text("Documents:\n{context}\n\nQuestion: {question}")
                        .param("context", aiContext)
                        .param("question", userQuestion))
                .call()
                .content();
    }
}