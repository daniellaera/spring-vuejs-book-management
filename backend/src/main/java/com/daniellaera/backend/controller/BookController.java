package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v3/book")
@Slf4j
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookService.getAllBooks(pageable);
    }

    @GetMapping("{bookId}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Integer bookId) {
        BookDTO book = bookService.findBookById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return ResponseEntity.ok().body(book);
    }

    @PostMapping
    public ResponseEntity<BookDTO> createBook(
            @RequestBody BookDTO book,
            @AuthenticationPrincipal User currentUser // Get the logged-in user
    ) {

        if (currentUser.getEmail() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = currentUser.getEmail();

        BookDTO createdBook = bookService.createBook(book, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }

    @DeleteMapping("{bookId}")
    public ResponseEntity<Void> deleteBook(@PathVariable Integer bookId, Authentication authentication) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build(); // HTTP 204: No Content
    }
}
