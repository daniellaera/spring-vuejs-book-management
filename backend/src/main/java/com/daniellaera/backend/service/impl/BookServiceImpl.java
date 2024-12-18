package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.dao.UserDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.BookService;
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
        List<CommentDTO> commentDTOList =
                (book.getComments() != null) ? book.getComments()
                        .stream()
                        .map(this::convertCommentToCommentDTO)
                        .toList() : List.of();
        bookDto.setComments(commentDTOList);

        UserDTO userDto = new UserDTO();
        userDto.setFullName(book.getCreatedBy().getFullName());
        bookDto.setUserDTO(userDto);

        return bookDto;
    }

    private CommentDTO convertCommentToCommentDTO(Comment comment) {
        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent(comment.getContent());
        commentDto.setAuthorFullName(comment.getUser().getFullName());
        return commentDto;
    }
}
