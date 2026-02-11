package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.properties.AiProperties;
import com.daniellaera.backend.service.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BookAiControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @Mock
    private AiProperties aiProperties;

    @Mock
    private AiRateLimiterService rateLimiter;

    @Mock
    private AiCacheService cache;

    @Mock
    private QdrantSearchService qdrantSearch;

    @Mock
    private BookVectorIndexService indexService;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.Builder chatClientBuilder;

    private BookAiController bookAiController;

    @BeforeEach
    void setUp() {
        given(chatClientBuilder.build()).willReturn(chatClient);
        bookAiController = new BookAiController(
                chatClientBuilder, bookService, aiProperties,
                rateLimiter, cache, qdrantSearch, indexService
        );
        mockMvc = MockMvcBuilders.standaloneSetup(bookAiController)
                .addFilter((request, response, chain) -> {
                    SecurityContextHolder.getContext().setAuthentication(
                            (Authentication) ((jakarta.servlet.http.HttpServletRequest) request).getUserPrincipal()
                    );
                    chain.doFilter(request, response);
                })
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void searchBooks_ReturnsMatchingBooks() throws Exception {
        BookDTO book = new BookDTO();
        book.setId(1);
        book.setTitle("Clean Code");
        book.setAuthor("Robert C. Martin");
        book.setGenre("Programming");

        given(qdrantSearch.searchSimilarBooks(anyString(), anyInt()))
                .willReturn(List.of(1, 2, 3));
        given(bookService.findBookById(1)).willReturn(Optional.of(book));
        given(bookService.findBookById(2)).willReturn(Optional.empty());
        given(bookService.findBookById(3)).willReturn(Optional.empty());

        mockMvc.perform(post("/api/v3/ai/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"clean code\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].title").value("Clean Code"));
    }

    @Test
    void searchBooks_EmptyQuery_ReturnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v3/ai/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchBooks_NoResults_ReturnsEmptyList() throws Exception {
        given(qdrantSearch.searchSimilarBooks(anyString(), anyInt()))
                .willReturn(List.of());

        mockMvc.perform(post("/api/v3/ai/books/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"query\": \"nonexistent book\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void indexBooks_ReturnsResult() throws Exception {
        given(indexService.indexAllBooks()).willReturn("Indexed 10 books");

        mockMvc.perform(post("/api/v3/ai/books/index")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Indexed 10 books"));

        verify(indexService, times(1)).indexAllBooks();
    }

    @Test
    void ask_CachedResponse_ReturnsCached() throws Exception {
        given(aiProperties.isEnabled()).willReturn(true);
        given(rateLimiter.isAllowed("alice@example.com")).willReturn(true);
        given(cache.get("what books do you have?")).willReturn("We have 10 books.");

        Authentication auth = new UsernamePasswordAuthenticationToken(
                "alice@example.com", null, List.of()
        );

        mockMvc.perform(post("/api/v3/ai/books/ask")
                        .principal(auth)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"question\": \"what books do you have?\"}")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answer").value("We have 10 books."))
                .andExpect(jsonPath("$.cached").value(true));

        verifyNoInteractions(qdrantSearch);
    }
}