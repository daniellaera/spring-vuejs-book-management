package com.daniellaera.backend.dao;

import com.fasterxml.jackson.annotation.JsonProperty;

public record BookAiView(
        @JsonProperty("id") Integer id,
        @JsonProperty("title") String title,
        @JsonProperty("author") String author,
        @JsonProperty("available") Boolean available
) {}