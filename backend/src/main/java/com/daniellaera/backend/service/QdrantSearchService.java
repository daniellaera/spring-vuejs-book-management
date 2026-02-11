package com.daniellaera.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class QdrantSearchService {

    private final WebClient webClient;

    @Value("${qdrant.url}")
    private String qdrantUrl;

    @Value("${ollama.api-url}")
    private String ollamaUrl;

    @Value("${qdrant.collection}")
    private String collection;

    public QdrantSearchService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    /**
     * Search Qdrant for books similar to the query.
     * Returns a list of book IDs ranked by relevance.
     */
    @SuppressWarnings("unchecked")
    public List<Integer> searchSimilarBooks(String query, int limit) {
        // Generate embedding for the query
        List<Double> queryEmbedding = getEmbedding(query);

        if (queryEmbedding == null || queryEmbedding.isEmpty()) {
            log.warn("Failed to generate embedding for query: {}", query);
            return List.of();
        }

        // Search Qdrant
        Map<String, Object> searchBody = Map.of(
                "vector", queryEmbedding,
                "limit", limit,
                "with_payload", true
        );

        try {
            Map<String, Object> response = webClient.post()
                    .uri(qdrantUrl + "/collections/" + collection + "/points/search")
                    .bodyValue(searchBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("result")) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("result");
                List<Integer> bookIds = new ArrayList<>();

                for (Map<String, Object> result : results) {
                    Map<String, Object> payload = (Map<String, Object>) result.get("payload");
                    if (payload != null && payload.containsKey("bookId")) {
                        bookIds.add((Integer) payload.get("bookId"));
                    }
                }

                log.info("Qdrant returned {} results for query: {}", bookIds.size(), query);
                return bookIds;
            }
        } catch (Exception e) {
            log.error("Qdrant search failed: {}", e.getMessage());
        }

        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Double> getEmbedding(String text) {
        try {
            Map<String, Object> response = webClient.post()
                    .uri(ollamaUrl + "/api/embeddings")
                    .bodyValue(Map.of(
                            "model", "nomic-embed-text",
                            "prompt", text
                    ))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.containsKey("embedding")) {
                return (List<Double>) response.get("embedding");
            }
        } catch (Exception e) {
            log.error("Ollama embedding failed: {}", e.getMessage());
        }
        return null;
    }
}