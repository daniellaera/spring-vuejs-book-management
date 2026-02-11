package com.daniellaera.backend.service;

import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.repository.BookRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class BookVectorIndexService {

    private final WebClient webClient;
    private final BookRepository bookRepository;

    @Value("${qdrant.url}")
    private String qdrantUrl;

    @Value("${ollama.api-url}")
    private String ollamaUrl;

    @Value("${qdrant.collection}")
    private String collection;

    public BookVectorIndexService(WebClient.Builder builder, BookRepository bookRepository) {
        this.webClient = builder.build();
        this.bookRepository = bookRepository;
    }

    public String indexAllBooks() {
        List<Book> books = bookRepository.findAll();
        log.info("Indexing {} books into Qdrant", books.size());

        List<Map<String, Object>> points = new ArrayList<>();

        for (Book book : books) {
            String text = buildBookText(book);
            List<Double> embedding = getEmbedding(text);

            if (embedding == null || embedding.size() != 768) {
                log.warn("Skipping book {}: invalid embedding (size {})",
                        book.getTitle(),
                        embedding == null ? "null" : embedding.size());
                continue;
            }

            points.add(Map.of(
                    "id", book.getId(),
                    "vector", embedding,
                    "payload", Map.of(
                            "bookId", book.getId(),
                            "title", book.getTitle(),
                            "author", book.getAuthor(),
                            "genre", book.getGenre() != null ? book.getGenre() : "Unknown"
                    )
            ));
        }

        if (points.isEmpty()) {
            return "No books indexed";
        }

        Map<String, Object> body = Map.of("points", points);

        String result;
        try {
            result = webClient.put()
                    .uri(qdrantUrl + "/collections/" + collection + "/points")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            log.error("Qdrant upsert failed: {}", e.getMessage());
            return "Failed to index books: " + e.getMessage();
        }

        log.info("Indexed {} books into Qdrant", points.size());
        return "Indexed " + points.size() + " books. Response: " + result;
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
                Object rawEmbedding = response.get("embedding");

                if (rawEmbedding instanceof List<?> rawList) {
                    List<Double> embedding = new ArrayList<>();
                    for (Object o : rawList) {
                        if (o instanceof Number n) {
                            embedding.add(n.doubleValue());
                        } else {
                            log.warn("Non-numeric value in embedding: {}", o);
                            return null;
                        }
                    }
                    return embedding;
                }
            }
        } catch (Exception e) {
            log.error("Ollama embedding failed: {}", e.getMessage());
        }
        return null;
    }

    private String buildBookText(Book book) {
        return String.join(". ",
                book.getTitle() + " by " + book.getAuthor(),
                "Genre: " + (book.getGenre() != null ? book.getGenre() : "Unknown"),
                "Description: " + (book.getDescription() != null ? book.getDescription() : "")
        ).trim();
    }
}