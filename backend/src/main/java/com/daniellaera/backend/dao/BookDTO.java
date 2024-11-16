package com.daniellaera.backend.dao;

import lombok.Data;

import java.util.List;

@Data
public class BookDTO {
    private Integer id;
    private String title;
    private String isbn;
    private String description;
    private UserDTO userDTO;
    private List<CommentDTO> comments;
}
