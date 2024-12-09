package com.daniellaera.backend.service.impl;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.dao.CommentDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.Comment;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    @Override
    public BookDTO createBook(BookDTO bookDTO) {
        Book book = convertBookDTOToBookEntity(bookDTO);
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


        return bookDto;
    }

    private CommentDTO convertCommentToCommentDTO(Comment comment) {
        CommentDTO commentDto = new CommentDTO();
        commentDto.setContent(comment.getContent());
        commentDto.setAuthorFullName(comment.getUser().getFullName());
        return commentDto;
    }
}
