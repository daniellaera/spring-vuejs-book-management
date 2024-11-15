package com.daniellaera.backend.controller;

import com.daniellaera.backend.dao.BookDTO;
import com.daniellaera.backend.dao.UserDTO;
import com.daniellaera.backend.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(MockitoExtension.class)
public class BookControllerTest {

    @InjectMocks
    private BookController bookController;

    private MockMvc mockMvc;

    @Mock
    private BookService bookService;

    @BeforeEach
    public void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
    }

    @Test
    void getAllBooks() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setFullName("John Doe");

        BookDTO bookDTO = new BookDTO();
        bookDTO.setIsbn("123456789");
        bookDTO.setTitle("Title");
        bookDTO.setDescription("Description");
        bookDTO.setUserDTO(userDTO);

        BookDTO bookDTO2 = new BookDTO();
        bookDTO2.setIsbn("123456789");
        bookDTO2.setTitle("Title");
        bookDTO2.setDescription("Description");
        bookDTO2.setUserDTO(userDTO);

        List<BookDTO> bookDTOList = Arrays.asList(bookDTO, bookDTO2);
        given(bookService.getAllBooks()).willReturn(bookDTOList);
        String reqBody = new ObjectMapper().writeValueAsString(bookDTOList);

        mockMvc.perform(get("/api/v3/book")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(reqBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Title"))
                .andExpect(jsonPath("$[0].description").value("Description"))
                .andExpect(jsonPath("$[0].isbn").value("123456789"));

        verify(bookService, times(1)).getAllBooks();
    }
}
