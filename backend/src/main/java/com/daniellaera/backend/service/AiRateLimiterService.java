package com.daniellaera.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AiRateLimiterService {

    private final Map<String, Integer> userRequests = new ConcurrentHashMap<>();

    // check if user can make a request
    public boolean isAllowed(String username) {
        return getRemainingRequests(username) > 0;
    }

    public void recordRequest(String username) {
        userRequests.merge(username, 1, Integer::sum);
    }

    public int getRemainingRequests(String username) {
        return Math.max(0, 20 - userRequests.getOrDefault(username, 0)); // 20 = dailyLimit
    }
}