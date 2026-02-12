package com.daniellaera.backend.repository;

import com.daniellaera.backend.dao.BookAiView;
import com.daniellaera.backend.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Integer>, BookRepositoryCustom {
    List<Book> findByBorrows_BorrowEndDateBeforeAndIsAvailableFalse(Date now);

    @Query("""
           SELECT new com.daniellaera.backend.dao.BookAiView(
               b.id, b.title, b.author, b.isAvailable, b.averageRating
           )
           FROM Book b
           WHERE b.id IN :ids
           """)
    List<BookAiView> findAllAiViewsByIds(@Param("ids") List<Integer> ids);
}
