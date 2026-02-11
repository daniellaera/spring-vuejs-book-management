package com.daniellaera.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v3/ai")
public class QdrantDebugController {

    private final WebClient webClient;

    @Value("${qdrant.url}")
    private String qdrantUrl;

    public QdrantDebugController(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    @GetMapping("/qdrant-health")
    public Mono<String> qdrantHealth() {
        return webClient.get()
                .uri(qdrantUrl)
                .retrieve()
                .bodyToMono(String.class);
    }
}