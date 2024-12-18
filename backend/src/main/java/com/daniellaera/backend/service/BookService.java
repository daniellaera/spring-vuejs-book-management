package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface BookService {
    Page<BookDTO> getAllBooks(Pageable pageable);

    Optional<BookDTO> findBookById(Integer bookId);

    BookDTO createBook(BookDTO book, String userEmail);
}
