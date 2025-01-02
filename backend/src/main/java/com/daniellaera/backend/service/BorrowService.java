package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BorrowDTO;
import org.springframework.stereotype.Service;

@Service
public interface BorrowService {
    BorrowDTO createBorrowByBookIdAndUserId(Integer bookId, String userEmail, BorrowDTO borrowDTO);

    BorrowDTO getBorrowByBookId(Integer bookId);
}
