package com.daniellaera.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    /*
     * Fixes: "Parameter 0 of constructor in com.daniellaera.backend.service.QdrantSearchService required
     * a bean of type 'org.springframework.web.reactive.function.client.WebClient$Builder' that could not be found." 
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
