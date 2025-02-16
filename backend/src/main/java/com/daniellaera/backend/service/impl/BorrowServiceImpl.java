package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.BorrowDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Borrow;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.BorrowService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BorrowServiceImpl implements BorrowService {

    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowRepository borrowRepository;

    @Autowired
    public BorrowServiceImpl(UserRepository userRepository, BookRepository bookRepository, BorrowRepository borrowRepository) {
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.borrowRepository = borrowRepository;
    }

    /**
     * Since you are modifying both Borrow and Book entities in a single operation,
     * it’s a good idea to wrap the method in a transactional context to avoid partial updates if an exception is thrown:
     */
    @Transactional
    @Override
    public BorrowDTO createBorrowByBookIdAndUserId(Integer bookId, String userEmail, BorrowDTO borrowDTO) {
        log.info("Loading user by username: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", userEmail);
                    return new UsernameNotFoundException("User not found with email: " + userEmail);
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", bookId);
                    return new EntityNotFoundException("Book not found with ID: " + bookId);
                });

        // Check if the book is already borrowed and not returned
        boolean isAlreadyBorrowed = book.getBorrows()
                .stream()
                .anyMatch(borrow -> !borrow.getIsReturned());

        if (isAlreadyBorrowed) {
            throw new IllegalStateException("Book is already borrowed and not yet returned.");
        }

        // Validate borrow dates
        if (borrowDTO.getBorrowStartDate().after(borrowDTO.getBorrowEndDate())) {
            throw new IllegalArgumentException("Borrow start date cannot be after the borrow end date.");
        }

        Borrow borrow = new Borrow();
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setIsReturned(false);
        borrow.setBorrowStartDate(borrowDTO.getBorrowStartDate());
        borrow.setBorrowEndDate(borrowDTO.getBorrowEndDate());

        Borrow savedBorrow = borrowRepository.save(borrow);
        log.info("Successfully created Borrow record for Book ID: {} and User ID: {}", bookId, user.getId());

        book.setIsAvailable(false);
        bookRepository.save(book);
        log.info("Setting Book with ID: {} to unavailable (isAvailable = false)", bookId);

        return convertBorrowToBorrowDTO(savedBorrow);
    }

    @Override
    public BorrowDTO getBorrowByBookId(Integer bookId) {
        Borrow borrow = borrowRepository.findByBookId(bookId).orElseThrow(
                () -> new EntityNotFoundException("Borrow not found with Book Id: " + bookId)
        );

        log.info("Successfully fetched Borrow record for Book ID: {}", bookId);

        return convertBorrowToBorrowDTO(borrow);
    }

    private BorrowDTO convertBorrowToBorrowDTO(Borrow savedBorrow) {

        return BorrowDTO.builder()
                .bookId(savedBorrow.getBook().getId())
                .userId(savedBorrow.getUser().getId())
                .isReturned(savedBorrow.getIsReturned())
                .borrowStartDate(savedBorrow.getBorrowStartDate())
                .borrowEndDate(savedBorrow.getBorrowEndDate())
                .build();

    }
}
