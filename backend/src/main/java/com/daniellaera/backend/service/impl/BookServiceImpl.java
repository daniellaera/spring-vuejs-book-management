package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.dao.UserDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll()
                .stream()
                .map(this::convertBookEntityToBookDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<BookDTO> findBookById(Integer bookId) {
        return bookRepository.findById(bookId).map(this::convertBookEntityToBookDto);
    }

    private BookDTO convertBookEntityToBookDto(Book book) {
        BookDTO bookDto = new BookDTO();
        bookDto.setId(book.getId());
        bookDto.setDescription(book.getDescription());
        bookDto.setTitle(book.getTitle());
        bookDto.setIsbn(book.getIsbn());
        List<CommentDTO> commentDTOList =
                (book.getComments() != null) ? book.getComments()
                        .stream()
                        .map(this::convertCommentToCommentDTO)
                        .toList() : List.of();
        bookDto.setComments(commentDTOList);

        UserDTO userDto = convertUserEntityToUserDto(book.getAuthor());
        bookDto.setUserDTO(userDto);

        return bookDto;
    }

    private CommentDTO convertCommentToCommentDTO(Comment comment) {
        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent(comment.getContent());
        commentDto.setAuthorFullName(comment.getUser().getFullName());
        return commentDto;
    }

    private UserDTO convertUserEntityToUserDto(User author) {
        UserDTO userDto = new UserDTO();
        userDto.setFullName(author.getFullName());
        return userDto;
    }
}
