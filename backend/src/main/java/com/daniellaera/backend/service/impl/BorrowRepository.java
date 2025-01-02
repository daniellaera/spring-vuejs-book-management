package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.model.Borrow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BorrowRepository extends JpaRepository<Borrow, Long> {
    Optional<Borrow> findByBookId(Integer bookId);
}
