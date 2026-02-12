package com.daniellaera.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class AiCacheService {

    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public String get(String question) {
        return cache.get(question);
    }

    public void put(String question, String answer) {
        cache.put(question, answer);
    }

    public void clear() {
        cache.clear();
        log.info("AI cache cleared");
    }
}