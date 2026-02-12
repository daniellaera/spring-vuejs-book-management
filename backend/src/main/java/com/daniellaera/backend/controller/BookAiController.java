package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookAiView;
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
                //.filter(b -> b.getTitle().toLowerCase().contains(queryLower))
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

    /**
     * The Heart of the RAG system: Assembles the prompt dynamically.
     */
    private String buildSmartPrompt(String question) {
        // 1. DYNAMIC COUNT: Tells Claude how big the library is
        long totalLibrarySize = bookService.getTotalBookCount();

        // 2. VECTOR SEARCH: Gets relevant IDs
        List<Integer> relevantIds = qdrantSearch.searchSimilarBooks(question, aiProperties.getSearchLimit());

        // 3. SLIM DATA FETCH: Converts IDs to Record Views
        List<BookAiView> books = bookService.getBooksAiOptimizedView(relevantIds);

        // 4. CONTEXT BUILDING: Uses Record Accessors (id(), title(), etc.)
        String bookContext = books.stream()
                .map(b -> String.format("ID: %d | Title: %s | Author: %s | Rating: %s | Available: %s",
                        b.id(), b.title(), b.author(),
                        b.averageRating() != null ? b.averageRating() : "N/A",
                        b.available() ? "Yes" : "No"))
                .collect(Collectors.joining("\n"));

        // 5. THE FINAL SYSTEM PROMPT
        return """
            You are a library assistant for a private collection.
            
            Current Library Metadata:
            - Total books in database: %d
            - The following %d books are the most relevant matches found for the user's query.
            
            Search Results Context:
            %s
            
            Instructions:
            1. If the user's question can be answered by the metadata above, do so concisely.
            2. If the user asks about the "entire catalog", mention there are %d books total.
            3. If a book is marked as 'Available: No', mention it is currently borrowed.
            4. Keep responses short and professional.
            """.formatted(totalLibrarySize, books.size(), bookContext, totalLibrarySize);
    }
}