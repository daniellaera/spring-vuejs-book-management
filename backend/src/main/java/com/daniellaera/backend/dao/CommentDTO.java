package com.daniellaera.backend.dao;

import lombok.Data;

@Data
public class CommentDTO {
    private String content;
    private String authorFullName;
}
