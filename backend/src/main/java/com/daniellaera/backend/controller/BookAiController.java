package com.daniellaera.backend.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.web.bind.annotation.*;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v3/ai/books")
public class BookAiController {

    private final ChatClient chatClient;

    // Inject the List of all ToolCallback beans automatically
    public BookAiController(ChatClient.Builder chatClientBuilder, List<ToolCallback> toolCallbacks) {
        this.chatClient = chatClientBuilder
                // Use this method for manually created ToolCallback beans
                .defaultToolCallbacks(toolCallbacks.toArray(new ToolCallback[0]))
                .build();
    }

    @PostMapping("/ask")
    public Map<String, Object> ask(@RequestBody Map<String, String> payload) {
        String question = payload.get("question");

        try {
            var response = chatClient.prompt()
                    .system("List the books found. Do not be silent.")
                    .user(question)
                    .call()
                    .chatResponse();

            String answer = response.getResult().getOutput().getText();

            // FAILSAFE: If the AI is silent but a tool was called,
            // the data is actually hidden in the conversation history.
            if (answer == null || answer.isBlank()) {
                return Map.of("answer", "The AI model is too weak to summarize. Try pulling llama3.1:8b or qwen2.5:7b (non-VL).");
            }

            return Map.of("answer", answer);

        } catch (Exception e) {
            return Map.of("error", e.getMessage());
        }
    }
}