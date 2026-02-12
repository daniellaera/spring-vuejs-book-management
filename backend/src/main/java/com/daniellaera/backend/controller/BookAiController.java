package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.properties.AiProperties;
import com.daniellaera.backend.service.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v3/ai/books")
public class BookAiController {

    private final ChatClient chatClient;
    private final BookService bookService;
    private final AiProperties aiProperties;
    private final AiRateLimiterService rateLimiter;
    private final AiCacheService cache;
    private final QdrantSearchService qdrantSearch;
    private final BookVectorIndexService indexService;

    public BookAiController(ChatClient.Builder chatClientBuilder,
                            BookService bookService,
                            AiProperties aiProperties,
                            AiRateLimiterService rateLimiter,
                            AiCacheService cache,
                            QdrantSearchService qdrantSearch,
                            BookVectorIndexService indexService) {
        this.bookService = bookService;
        this.aiProperties = aiProperties;
        this.rateLimiter = rateLimiter;
        this.cache = cache;
        this.qdrantSearch = qdrantSearch;
        this.indexService = indexService;
        this.chatClient = chatClientBuilder.build();
    }

    @PostMapping("/search")
    public List<BookDTO> searchBooks(@RequestBody Map<String, String> payload) {
        String query = payload.get("query");
        if (query == null || query.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Query cannot be empty");
        }

        List<Integer> ids = qdrantSearch.searchSimilarBooks(query, 10);
        String queryLower = query.toLowerCase();

        return ids.stream()
                .map(id -> bookService.findBookById(id).orElse(null)) // returns Optional<BookDTO>
                .filter(Objects::nonNull)
                .filter(b -> b.getTitle().toLowerCase().contains(queryLower))
                .toList();
    }

    @GetMapping(value = "/ask/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> askStream(@RequestParam String question, Authentication auth) {
        validateRequest(auth);

        // Check cache
        String cached = cache.get(question);
        if (cached != null) {
            return Flux.just(cached);
        }

        rateLimiter.recordRequest(auth.getName());

        StringBuilder fullResponse = new StringBuilder();

        return chatClient.prompt()
                .system(buildSmartPrompt(question))
                .user(question)
                .stream()
                .content()
                .doOnNext(fullResponse::append)
                .doOnComplete(() -> cache.put(question, fullResponse.toString()));
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody Map<String, String> payload, Authentication auth) {
        validateRequest(auth);

        String question = payload.get("question");

        // Check cache
        String cached = cache.get(question);
        if (cached != null) {
            return Map.of("answer", cached, "cached", true);
        }

        rateLimiter.recordRequest(auth.getName());

        var response = chatClient.prompt()
                .system(buildSmartPrompt(question))
                .user(question)
                .call()
                .chatResponse();

        String answer = response.getResult().getOutput().getText();
        cache.put(question, answer);

        return Map.of("answer", answer);
    }

    @PostMapping("/index")
    public Map<String, String> indexBooks() {
        String result = indexService.indexAllBooks();
        return Map.of("result", result);
    }

    @GetMapping("/remaining")
    public Map<String, Object> getRemainingRequests(Authentication auth) {
        return Map.of(
                "remaining", rateLimiter.getRemainingRequests(auth.getName()),
                "dailyLimit", aiProperties.getDailyLimit()
        );
    }

    private void validateRequest(Authentication auth) {
        if (!aiProperties.isEnabled()) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "AI feature is disabled");
        }
        if (!rateLimiter.isAllowed(auth.getName())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Daily AI limit reached");
        }
    }

    private String buildSmartPrompt(String question) {
        List<Integer> relevantIds = qdrantSearch.searchSimilarBooks(question, 15);

        List<BookDTO> books;
        if (!relevantIds.isEmpty()) {
            books = relevantIds.stream()
                    .map(id -> bookService.findBookById(id).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
        } else {
            books = bookService.getAllBooks(
                    PageRequest.of(0, 100), null
            ).getContent();
        }

        String bookContext = books.stream()
                .map(b -> String.join(" | ",
                        "ID: " + b.getId(),
                        "Title: " + b.getTitle(),
                        "Author: " + b.getAuthor(),
                        "Genre: " + b.getGenre(),
                        "Available: " + (Boolean.TRUE.equals(b.getIsAvailable()) ? "Yes" : "No"),
                        "Rating: " + (b.getAverageRating() != null ? b.getAverageRating() : "N/A"),
                        "Comments: " + (b.getComments() != null ? b.getComments().size() : 0)
                ))
                .collect(Collectors.joining("\n"));

        return """
                You are a library assistant inside a small chat widget.
                Rules:
                - Be very concise, short answers
                - No markdown, no bold, no bullet points
                - Use numbered lists, one book per line
                - Format each book as: "1. Title - Author (Rating: X)"
                - Only include details the user asked about
                - Keep answers under 10 lines when possible
                
                Book catalog:
                %s
                """.formatted(bookContext);
    }
}