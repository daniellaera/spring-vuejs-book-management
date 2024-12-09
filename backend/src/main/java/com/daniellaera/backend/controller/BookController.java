package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v3/book")
public class BookController {

    private final BookService bookService;

    @Autowired
    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<List<BookDTO>> getBooks() {
        List<BookDTO> bookList = bookService.getAllBooks();
        return ResponseEntity.ok().body(bookList);
    }

    @GetMapping("{bookId}")
    public ResponseEntity<BookDTO> getBook(@PathVariable Integer bookId) {
        BookDTO book = bookService.findBookById(bookId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
        return ResponseEntity.ok().body(book);
    }

    @PostMapping
    public ResponseEntity<BookDTO> createBook(@RequestBody BookDTO book) {
        BookDTO createdBook = bookService.createBook(book);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBook);
    }
}
