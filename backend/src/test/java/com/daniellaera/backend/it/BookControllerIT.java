package com.daniellaera.backend.it;

import com.daniellaera.backend.model.Book;
import com.daniellaera.backend.repository.BookRepository;
import org.flywaydb.test.annotation.FlywayTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test-containers-flyway")
@FlywayTest
@Testcontainers
public class BookControllerIT {

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            "postgres:16-alpine"
    );

    @BeforeAll
    static void beforeAll() {
        postgres.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setup() {
        bookRepository.deleteAll();

        Book book1 = new Book();
        book1.setTitle("Title 1");
        book1.setDescription("Description 1");
        book1.setIsbn("978-1234567890");
        book1.setAuthor("Thomas H. Cormen");
        book1.setGenre("Fiction");

        LocalDate publishedLocalDate = LocalDate.of(2020, 5, 15); // May 15, 2020
        book1.setPublishedDate(Date.from(publishedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        book1.setCreatedDate(new Date());

        Book book2 = new Book();
        book2.setTitle("Title 2");
        book2.setDescription("Description 2");
        book2.setIsbn("978-0262033848");
        book2.setAuthor("Joshua Bloch");
        book2.setGenre("Programming");
        book2.setPublishedDate(Date.from(publishedLocalDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        book2.setCreatedDate(new Date());

        bookRepository.saveAll(Arrays.asList(book1, book2));
    }

    @Test
    void getBooks_ReturnsAllBooks() throws Exception {
        mockMvc.perform(get("/api/v3/book")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"));
    }

    @Test
    void getBooks_WhenNoBooks_ReturnsEmptyArray() throws Exception {
        bookRepository.deleteAll();

        mockMvc.perform(get("/api/v3/book")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    void getBooks_CheckSingleFieldContent() throws Exception {
        mockMvc.perform(get("/api/v3/book")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content[0].title").value("Title 1"))
                .andExpect(jsonPath("$.content[1].title").value("Title 2"));
    }

    @Test
    void createBook_CreatesBookSuccessfully() throws Exception {
        // Arrange
        String bookJson = """
        {
            "title": "New Book Title",
            "description": "New Book Description",
            "isbn": "978-0000000000",
            "author": "New Author",
            "genre": "Science Fiction",
            "publishedDate": "2023-01-01"
        }
        """;

        // Act & Assert
        mockMvc.perform(post("/api/v3/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title").value("New Book Title"))
                .andExpect(jsonPath("$.description").value("New Book Description"))
                .andExpect(jsonPath("$.isbn").value("978-0000000000"))
                .andExpect(jsonPath("$.author").value("New Author"))
                .andExpect(jsonPath("$.genre").value("Science Fiction"))
                .andExpect(jsonPath("$.publishedDate").value("2023-01-01T00:00:00.000+00:00")); // Ensure the date matches

        // Verify that the book is stored in the database
        assertThat(bookRepository.findAll()).hasSize(3); // Two books from setup + one new book
        Book savedBook = bookRepository.findAll().stream()
                .filter(book -> "New Book Title".equals(book.getTitle()))
                .findFirst()
                .orElseThrow();
        assertThat(savedBook.getTitle()).isEqualTo("New Book Title");
        assertThat(savedBook.getPublishedDate()).isNotNull();
    }
}
