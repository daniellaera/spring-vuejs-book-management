package com.daniellaera.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_book", indexes = {
        @Index(name = "idx_book_title", columnList = "title"),
        @Index(name = "idx_book_author", columnList = "author"),
        @Index(name = "idx_book_isbn", columnList = "isbn"),
        @Index(name = "idx_book_is_available", columnList = "isAvailable"),
        @Index(name = "idx_book_genre", columnList = "genre"),
        @Index(name = "idx_book_created_date", columnList = "createdDate"),
        // Composite index for common search patterns
        @Index(name = "idx_book_title_author", columnList = "title, author"),
        @Index(name = "idx_book_available_genre", columnList = "isAvailable, genre")
})
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(unique = true, length = 20)
    private String isbn;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false, length = 255)
    private String author;

    @Column(length = 100)
    private String genre;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    // todo check Temporal deprecation
    @Temporal(TemporalType.DATE) // Represents the book's real-world publication date
    private Date publishedDate;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Comment> comments;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Rating> ratings;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Borrow> borrows;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    private Double averageRating;

    @Column(nullable = false)
    private Boolean isAvailable = true;
}
