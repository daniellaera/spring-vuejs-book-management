package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.dao.UserDTO;
import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.model.User;
import com.daniellaera.backend.repository.BookRepository;
import com.daniellaera.backend.repository.BookRepositoryCustom;
import com.daniellaera.backend.repository.UserRepository;
import com.daniellaera.backend.service.impl.BookServiceImpl;
import com.daniellaera.backend.service.impl.BorrowRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookServiceTest {

    private BookServiceImpl bookService;

    // todo is it better for test or ... ??
    // @InjectMocks
    // private BookServiceImpl bookServiceImpl;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookRepositoryCustom bookRepositoryCustom;

    @Mock
    private BorrowRepository borrowRepository;

    @Mock
    private AiCacheService aiCacheService;

    @BeforeEach
    public void setUp() {
        // to remove in case we @InjectMocks of bookServiceImpl
        bookService = new BookServiceImpl(bookRepository, userRepository, borrowRepository, aiCacheService);
    }

    @Test
    void getAllBooks_ShouldReturnPagedBooks() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setId(1);
        bookDTO.setTitle("Test Book");

        Page<BookDTO> page = new PageImpl<>(List.of(bookDTO));
        when(bookRepository.findAllBooksOptimized(any(PageRequest.class), any())).thenReturn(page);

        Page<BookDTO> result = bookService.getAllBooks(PageRequest.of(0, 10), null);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Test Book");
        verify(bookRepository, times(1)).findAllBooksOptimized(any(Pageable.class), any());
    }

    @Test
    void findBookById_ShouldReturnBookIfExists() {
        BookDTO bookDTO = BookDTO.builder()
                .id(1)
                .title("Test Book")
                .userDTO(UserDTO.builder()
                        .id(1)
                        .fullName("John Doe")
                        .build())
                .build();

        when(bookRepository.findBookByIdOptimized(1)).thenReturn(Optional.of(bookDTO));

        Optional<BookDTO> result = bookService.findBookById(1);

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Test Book");
        assertThat(result.get().getUserDTO().getFullName()).isEqualTo("John Doe");
        verify(bookRepository, times(1)).findBookByIdOptimized(1);
    }

    @Test
    void findBookById_ShouldReturnEmptyIfNotFound() {
        when(bookRepository.findBookByIdOptimized(1)).thenReturn(Optional.empty());

        Optional<BookDTO> result = bookService.findBookById(1);

        assertThat(result).isEmpty();
        verify(bookRepository, times(1)).findBookByIdOptimized(1);
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

    @Test
    void deleteBook_ShouldDeleteBookSuccessfully() {
        int bookId = 1;

        Book book = new Book();
        book.setId(bookId);
        book.setTitle("Test Book");

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        bookService.deleteBook(bookId);

        verify(bookRepository, times(1)).delete(book);
    }

    @Test
    void deleteBook_ShouldThrowExceptionIfBookNotFound() {
        int bookId = 1;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        Exception exception = org.junit.jupiter.api.Assertions.assertThrows(
                EntityNotFoundException.class,
                () -> bookService.deleteBook(bookId)
        );

        assertThat(exception.getMessage()).isEqualTo("Book not found with id: " + bookId);
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test
    void getTotalBookCount_ShouldReturnCount() {
        when(bookRepository.count()).thenReturn(42L);

        long count = bookService.getTotalBookCount();

        assertThat(count).isEqualTo(42L);
        verify(bookRepository, times(1)).count();
    }

    @Test
    void getAllBooks_ShouldPassSearchParam() {
        Page<BookDTO> page = new PageImpl<>(List.of());
        when(bookRepository.findAllBooksOptimized(any(Pageable.class), eq("harry")))
                .thenReturn(page);

        Page<BookDTO> result = bookService.getAllBooks(PageRequest.of(0, 5), "harry");

        assertThat(result).isNotNull();
        verify(bookRepository).findAllBooksOptimized(any(Pageable.class), eq("harry"));
    }

    @Test
    void getAllBooks_ShouldNotTouchAiCache() {
        Page<BookDTO> page = new PageImpl<>(List.of(new BookDTO()));
        when(bookRepository.findAllBooksOptimized(any(Pageable.class), any()))
                .thenReturn(page);

        bookService.getAllBooks(PageRequest.of(0, 10), null);

        verifyNoInteractions(aiCacheService);
    }

    @Test
    void createBook_ShouldClearAiCache() {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Test Book");

        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");

        Book book = new Book();
        book.setId(1);
        book.setTitle("Test Book");
        book.setCreatedBy(user); // 👈 THIS fixes the NPE

        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(bookRepository.save(any(Book.class)))
                .thenReturn(book);

        // Act
        bookService.createBook(bookDTO, "test@example.com");

        // Assert
        verify(aiCacheService, times(1)).clear();
    }

    @Test
    void deleteBook_ShouldClearAiCache() {
        Book book = new Book();
        book.setId(1);

        when(bookRepository.findById(1)).thenReturn(Optional.of(book));

        // Act
        bookService.deleteBook(1);

        // Assert
        verify(aiCacheService, times(1)).clear();
    }
}
