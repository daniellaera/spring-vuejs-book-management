package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.*;
import com.daniellaera.backend.model.*;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.BookService;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, UserRepository userRepository) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Page<BookDTO> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(this::convertBookEntityToBookDto);
    }

    @Override
    public Optional<BookDTO> findBookById(Integer bookId) {
        return bookRepository.findById(bookId).map(this::convertBookEntityToBookDto);
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
        List<CommentDTO> commentDTOList =
                (book.getComments() != null) ? book.getComments()
                        .stream()
                        .map(this::convertCommentToCommentDTO)
                        .toList() : List.of();

        // ratings
        List<RatingDTO> ratingDTOList =
                (book.getRatings() != null) ? book.getRatings()
                        .stream()
                        .map(this::convertRatingToRatingDTO)
                        .toList() : List.of();

        // borrow
        BorrowDTO borrowDTO = (book.getBorrow() != null) ? convertBorrowEntityToBorrowDTO(book.getBorrow()) : null;
        bookDto.setBorrow(borrowDTO);

        bookDto.setComments(commentDTOList);
        bookDto.setRatings(ratingDTOList);
        bookDto.setAverageRating(book.getAverageRating());

        UserDTO userDto = new UserDTO();
        userDto.setId(book.getCreatedBy().getId());
        userDto.setFullName(book.getCreatedBy().getFullName());
        bookDto.setUserDTO(userDto);

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
