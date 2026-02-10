package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BookAiView;
import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.dao.BookListProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public interface BookService {
    Page<BookDTO> getAllBooks(Pageable pageable, String search);

    Optional<BookDTO> findBookById(Integer bookId);

    BookDTO createBook(BookDTO book, String userEmail);

    void deleteBook(Integer bookId);

    void updateExpiredBookStatus();

    // AI / fast listing
    Page<BookAiView> getBookList(Pageable pageable);

    // NEW: Search books by keyword in title or author
    List<BookAiView> searchByKeyword(String keyword);
}
