package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Test
    void getAllBooks_ShouldReturnPagedBooks() {
        // Arrange
        User user = new User();
        user.setId(1);
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("password");

        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setCreatedBy(user); // Set createdBy user

        Page<Book> page = new PageImpl<>(List.of(book));
        when(bookRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        Page<BookDTO> result = bookService.getAllBooks(PageRequest.of(0, 10));

        // Assert
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Book");
        assertThat(result.getContent().getFirst().getUserDTO().getFullName()).isEqualTo("John Doe"); // Verify createdBy
        verify(bookRepository, times(1)).findAll(any(PageRequest.class));
    }

    @Test
    void findBookById_ShouldReturnBookIfExists() {
        User user = new User();
        user.setId(1);
        user.setEmail("john.doe@example.com");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setPassword("password");
        // Arrange
        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setCreatedBy(user); // Set createdBy user
        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        // Act
        Optional<BookDTO> result = bookService.findBookById(1);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findById(1);
    }

    @Test
    void findBookById_ShouldReturnEmptyIfNotFound() {
        // Arrange
        when(bookRepository.findById(1)).thenReturn(Optional.empty());

        // Act
        Optional<BookDTO> result = bookService.findBookById(1);

        // Assert
        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findById(1);
    }

    @Test
    void createBook_ShouldSaveBookSuccessfully() {
        // Arrange
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Test Book");
        bookDTO.setAuthor("Test Author");

        User user = new User();
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setId(1);
        user.setEmail("test@example.com");

        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setCreatedBy(user);

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        BookDTO result = bookService.createBook(bookDTO, "test@example.com");

        // Assert
        assertThat(result.getTitle()).isEqualTo("Test Book");
        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());
        Book capturedBook = bookCaptor.getValue();
        assertThat(capturedBook.getTitle()).isEqualTo("Test Book");
        assertThat(capturedBook.getCreatedBy().getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void createBook_ShouldThrowExceptionIfUserNotFound() {
        // Arrange
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Test Book");
        bookDTO.setAuthor("Test Author");

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                UsernameNotFoundException.class,
                () -> bookService.createBook(bookDTO, "nonexistent@example.com")
        );

        assertThat(exception.getMessage()).isEqualTo("User not found with email: nonexistent@example.com");
        verify(bookRepository, never()).save(any(Book.class));
    }
}
