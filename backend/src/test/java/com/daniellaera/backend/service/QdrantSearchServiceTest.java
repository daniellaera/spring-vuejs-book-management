package com.daniellaera.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class QdrantSearchServiceTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private QdrantSearchService qdrantSearchService;

    @BeforeEach
    void setUp() throws Exception {
        WebClient.Builder builder = mock(WebClient.Builder.class);
        given(builder.build()).willReturn(webClient);

        qdrantSearchService = new QdrantSearchService(builder);

        setField("qdrantUrl", "http://localhost:6333");
        setField("collection", "books");
        setField("ollamaUrl", "http://localhost:11434");
    }

    private void setField(String name, String value) throws Exception {
        var field = QdrantSearchService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(qdrantSearchService, value);
    }

    @Test
    void searchSimilarBooks_OllamaFails_ReturnsEmptyList() {
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodyUriSpec);
        doReturn(requestHeadersSpec).when(requestBodyUriSpec).bodyValue(any());
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(Map.class))
                .willReturn(Mono.error(new RuntimeException("Ollama unavailable")));

        List<Integer> results = qdrantSearchService.searchSimilarBooks("test", 5);

        assertThat(results).isEmpty();
    }

    @Test
    void searchSimilarBooks_EmptyEmbedding_ReturnsEmptyList() {
        given(webClient.post()).willReturn(requestBodyUriSpec);
        given(requestBodyUriSpec.uri(anyString())).willReturn(requestBodyUriSpec);
        doReturn(requestHeadersSpec).when(requestBodyUriSpec).bodyValue(any());
        given(requestHeadersSpec.retrieve()).willReturn(responseSpec);
        given(responseSpec.bodyToMono(Map.class))
                .willReturn(Mono.just(Map.of("embedding", List.of())));

        List<Integer> results = qdrantSearchService.searchSimilarBooks("test", 5);

        assertThat(results).isEmpty();
    }
}