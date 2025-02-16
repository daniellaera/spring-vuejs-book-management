package com.daniellaera.backend.repository;

import com.daniellaera.backend.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer> {
    List<Book> findByBorrows_BorrowEndDateBeforeAndIsAvailableFalse(Date now);
}
