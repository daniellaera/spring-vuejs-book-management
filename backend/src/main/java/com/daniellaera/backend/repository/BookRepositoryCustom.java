package com.daniellaera.backend.repository;

import com.daniellaera.backend.dao.BookDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookRepositoryCustom {
    Page<BookDTO> findAllBooksOptimized(Pageable pageable, String search);

    Optional<BookDTO> findBookByIdOptimized(Integer bookId);
}
