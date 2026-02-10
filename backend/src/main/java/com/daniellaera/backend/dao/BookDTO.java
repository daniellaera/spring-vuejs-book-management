package com.daniellaera.backend.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Integer id;
    private String title;
    private String isbn;
    private String description;
    private String author;
    private String genre;
    private Date createdDate;
    private Date publishedDate;
    private List<CommentDTO> comments;
    private List<RatingDTO> ratings;
    private BorrowDTO borrow;
    private Double averageRating;
    private UserDTO userDTO;
    private Boolean isAvailable;
}
