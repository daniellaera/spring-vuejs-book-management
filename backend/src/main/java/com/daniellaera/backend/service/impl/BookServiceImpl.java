package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.*;
import com.daniellaera.backend.model.*;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.AiCacheService;
import com.daniellaera.backend.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BorrowRepository borrowRepository;
    private final AiCacheService aiCacheService;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository,
                           UserRepository userRepository,
                           BorrowRepository borrowRepository, AiCacheService aiCacheService) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.borrowRepository = borrowRepository;
        this.aiCacheService = aiCacheService;
    }

    @Override
    public long getTotalBookCount() {
        return bookRepository.count();
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable, String search) {
        return bookRepository.findAllBooksOptimized(pageable, search);
    }

    @Override
    public Optional<BookDTO> findBookById(Integer bookId) {
        return bookRepository.findBookByIdOptimized(bookId);
    }

    @Override
    public BookDTO createBook(BookDTO bookDTO, String userEmail) {
        log.info("Loading user by username: {}", userEmail);

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.error("User not found with email: {}", userEmail);
                    return new UsernameNotFoundException("User not found with email: " + userEmail);
                });

        Book book = convertBookDTOToBookEntity(bookDTO);
        book.setCreatedBy(user);

        Book savedBook = bookRepository.save(book);

        aiCacheService.clear();

        return convertBookEntityToBookDto(savedBook);
    }

    @Override
    public void deleteBook(Integer bookId) {
        Book book = bookRepository.findById(bookId).orElseThrow(() -> {
            log.error("Book not found with id: {}", bookId);
            return new EntityNotFoundException("Book not found with id: " + bookId);
        });
        bookRepository.delete(book);
        log.info("Book with id: {} deleted", bookId);
        aiCacheService.clear();
    }

    @Override
    public List<BookAiView> getBooksAiOptimizedView(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return bookRepository.findAllAiViewsByIds(ids);
    }

    @Override
    @Transactional
    public void updateExpiredBookStatus() {
        // Use tomorrow midnight so borrows expiring TODAY are included
        Date cutoffDate = Date.from(
                LocalDate.now().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
        );

        List<Book> expiredBorrowBooks = bookRepository.findByBorrows_BorrowEndDateBeforeAndIsAvailableFalse(cutoffDate);

        if (expiredBorrowBooks.isEmpty()) {
            log.info("No expired books to update.");
            return;
        }

        int updatedCount = 0;

        for (Book book : expiredBorrowBooks) {
            Borrow activeBorrow = book.getBorrows()
                    .stream()
                    .filter(brw -> !brw.getIsReturned())
                    .findFirst()
                    .orElse(null);

            if (activeBorrow != null) {
                log.info("Returning '{}' (ID: {}) - borrow expired on {}",
                        book.getTitle(), book.getId(), activeBorrow.getBorrowEndDate());

                activeBorrow.setIsReturned(true);
                book.setIsAvailable(true);

                borrowRepository.save(activeBorrow);
                bookRepository.save(book);
                updatedCount++;
            } else {
                // Book unavailable but all borrows returned — fix inconsistency
                log.warn("Book '{}' (ID: {}) is unavailable but has no active borrows - fixing",
                        book.getTitle(), book.getId());
                book.setIsAvailable(true);
                bookRepository.save(book);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            aiCacheService.clear();
            log.info("Updated {} books to available.", updatedCount);
        } else {
            log.info("Found {} expired borrows but none needed updating.", expiredBorrowBooks.size());
        }
    }

    private Book convertBookDTOToBookEntity(BookDTO bookDTO) {
        Book book = new Book();

        List<Comment> comments = Optional.ofNullable(bookDTO.getComments())
                .orElse(List.of())
                .stream()
                .map(commentDTO -> {
                    Comment comment = convertCommentDTOToCommentEntity(commentDTO);
                    comment.setBook(book);
                    return comment;
                })
                .toList();

        book.setComments(comments);
        book.setAuthor(bookDTO.getAuthor());
        book.setTitle(bookDTO.getTitle());
        book.setIsbn(bookDTO.getIsbn());
        book.setGenre(bookDTO.getGenre());
        book.setDescription(bookDTO.getDescription());
        book.setPublishedDate(bookDTO.getPublishedDate());
        book.setAverageRating(0.0);

        return book;
    }

    private Comment convertCommentDTOToCommentEntity(CommentDTO commentDTO) {
        Comment comment = new Comment();
        comment.setContent(commentDTO.getContent());
        return comment;
    }

    private BookDTO convertBookEntityToBookDto(Book book) {
        BookDTO bookDto = new BookDTO();
        bookDto.setId(book.getId());
        bookDto.setDescription(book.getDescription());
        bookDto.setTitle(book.getTitle());
        bookDto.setIsbn(book.getIsbn());
        bookDto.setAuthor(book.getAuthor());
        bookDto.setGenre(book.getGenre());
        bookDto.setCreatedDate(book.getCreatedDate());
        bookDto.setPublishedDate(book.getPublishedDate());

        // comments
        List<CommentDTO> commentDTOList = (book.getComments() != null) ? book.getComments()
                        .stream()
                        .map(this::convertCommentToCommentDTO)
                        .toList() : List.of();

        // ratings
        List<RatingDTO> ratingDTOList = (book.getRatings() != null) ? book.getRatings()
                        .stream()
                        .map(this::convertRatingToRatingDTO)
                        .toList() : List.of();

        // borrow
        BorrowDTO borrowDTO = Optional.ofNullable(book.getBorrows())
                .orElse(List.of()) // Provide an empty list if `null`
                .stream()
                .filter(brw -> Boolean.FALSE.equals(brw.getIsReturned()))
                .findFirst()
                .map(this::convertBorrowEntityToBorrowDTO)
                .orElse(null);

        // BorrowDTO borrowDTO = (book.getBorrow() != null) ? convertBorrowEntityToBorrowDTO(book.getBorrow()) : null;
        bookDto.setBorrow(borrowDTO);

        bookDto.setComments(commentDTOList);
        bookDto.setRatings(ratingDTOList);
        bookDto.setAverageRating(book.getAverageRating());

        UserDTO userDto = new UserDTO();
        userDto.setId(book.getCreatedBy().getId());
        userDto.setFullName(book.getCreatedBy().getFullName());
        bookDto.setUserDTO(userDto);
        bookDto.setIsAvailable(book.getIsAvailable());

        return bookDto;
    }

    private BorrowDTO convertBorrowEntityToBorrowDTO(Borrow borrow) {
        return BorrowDTO.builder()
                .bookId(borrow.getBook().getId())
                .userId(borrow.getUser().getId())
                .isReturned(borrow.getIsReturned())
                .borrowStartDate(borrow.getBorrowStartDate())
                .borrowEndDate(borrow.getBorrowEndDate())
                .build();
    }

    private RatingDTO convertRatingToRatingDTO(Rating rating) {
        RatingDTO ratingDTO = new RatingDTO();
        ratingDTO.setScore(rating.getScore());
        ratingDTO.setUserId(rating.getUser().getId());

        return ratingDTO;
    }

    private CommentDTO convertCommentToCommentDTO(Comment comment) {
        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent(comment.getContent());
        commentDto.setAuthorFullName(comment.getUser().getFullName());
        return commentDto;
    }
}
