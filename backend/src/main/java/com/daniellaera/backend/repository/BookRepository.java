package com.daniellaera.backend.repository;

import com.daniellaera.backend.dao.BookListProjection;
import com.daniellaera.backend.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, BookRepositoryCustom {
    List<Book> findByBorrows_BorrowEndDateBeforeAndIsAvailableFalse(Date now);

    @Query("""
        SELECT b.id AS id,
           b.title AS title,
           b.author AS author,
           b.isAvailable AS isAvailable
        FROM Book b
        """)
    Page<BookListProjection> findBookList(Pageable pageable);

    // Derived query: finds books where title OR author contains the string (case-insensitive)
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);
}
