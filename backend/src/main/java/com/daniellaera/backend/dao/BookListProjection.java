package com.daniellaera.backend.dao;

public interface BookListProjection {
    Integer getId();
    String getTitle();
    String getAuthor();
    Boolean getIsAvailable();
}