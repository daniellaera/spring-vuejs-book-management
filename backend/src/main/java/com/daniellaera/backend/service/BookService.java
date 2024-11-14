package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BookDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface BookService {
    List<BookDTO> getAllBooks();

    Optional<BookDTO> findBookById(Integer bookId);
}
