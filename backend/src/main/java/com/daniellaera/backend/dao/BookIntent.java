package com.daniellaera.backend.dao;

public record BookIntent(
        String intent,
        int page,
        int size,
        Integer bookId
) {}