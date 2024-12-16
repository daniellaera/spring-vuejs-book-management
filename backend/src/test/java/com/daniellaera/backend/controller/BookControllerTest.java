package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.refEq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()) // Register Pageable resolver
                .build();
    }

    @Test
    void getAllBooks() throws Exception {
        // Sample books
        BookDTO bookDTO1 = new BookDTO();
        bookDTO1.setIsbn("123456789");
        bookDTO1.setTitle("Title");
        bookDTO1.setDescription("Description");
        bookDTO1.setAuthor("Thomas H. Cormen");
        bookDTO1.setGenre("Fiction");

        BookDTO bookDTO2 = new BookDTO();
        bookDTO2.setIsbn("987654321");
        bookDTO2.setTitle("Another Title");
        bookDTO2.setDescription("Another Description");
        bookDTO2.setAuthor("John Doe");
        bookDTO2.setGenre("Non-Fiction");

        // Mock Pageable response
        Pageable pageable = PageRequest.of(0, 5, Sort.by("title").ascending());
        Page<BookDTO> bookDTOPage = new PageImpl<>(Arrays.asList(bookDTO1, bookDTO2), pageable, 2);

        // Mock the service response
        given(bookService.getAllBooks(any(Pageable.class))).willReturn(bookDTOPage);

        // Perform the GET request with pagination params
        mockMvc.perform(get("/api/v3/book")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "title,asc")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalElements").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.size").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(0))
                .andExpect(MockMvcResultMatchers.jsonPath("$.sort").exists());
    }

    @Test
    void createBook_success() throws Exception {
        BookDTO bookDTO = new BookDTO();
        bookDTO.setIsbn("123456789");
        bookDTO.setTitle("Title");
        bookDTO.setDescription("Description");
        bookDTO.setAuthor("Thomas H. Cormen");
        bookDTO.setGenre("Fiction");

        BookDTO createdBookDTO = new BookDTO();
        createdBookDTO.setId(1);
        createdBookDTO.setIsbn(bookDTO.getIsbn());
        createdBookDTO.setTitle(bookDTO.getTitle());
        createdBookDTO.setDescription(bookDTO.getDescription());
        createdBookDTO.setAuthor(bookDTO.getAuthor());
        createdBookDTO.setGenre(bookDTO.getGenre());

        given(bookService.createBook(refEq(bookDTO))).willReturn(createdBookDTO);
        String reqBody = new ObjectMapper().writeValueAsString(bookDTO);

        mockMvc.perform(post("/api/v3/book")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.isbn").value("123456789"))
                .andExpect(jsonPath("$.title").value("Title"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.author").value("Thomas H. Cormen"))
                .andExpect(jsonPath("$.genre").value("Fiction"));

        verify(bookService, times(1)).createBook(refEq(bookDTO));
    }
}
