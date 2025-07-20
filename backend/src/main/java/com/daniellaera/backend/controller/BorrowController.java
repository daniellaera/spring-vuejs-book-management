package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BorrowDTO;
import com.daniellaera.backend.service.BorrowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v3/borrow")
public class BorrowController {

    private final BorrowService borrowService;

    @Autowired
    public BorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BorrowDTO> getBorrowByBookId(@PathVariable Integer bookId) {
        BorrowDTO borrowDTO = borrowService.getBorrowByBookId(bookId);

        return new ResponseEntity<>(borrowDTO, HttpStatus.OK);
    }

    @PreAuthorize("hasAuthority('USER')")
    @PostMapping("/{bookId}")
    public ResponseEntity<BorrowDTO> createBorrow(
            Authentication authentication,
            @PathVariable Integer bookId,
            @RequestBody BorrowDTO borrowDTO) {

        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String userEmail = authentication.getName();

        return ResponseEntity.ok(borrowService.createBorrowByBookIdAndUserId(bookId, userEmail, borrowDTO));
    }
}
