package com.daniellaera.backend.service;

import com.daniellaera.backend.dao.BookAiView;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class BookAiTools {

    private final BookService bookService;

    public BookAiTools(BookService bookService) {
        this.bookService = bookService;
    }

    @Tool(description = "Search for books by a specific keyword in the title or author")
    public String searchBooks(@ToolParam(description = "The keyword to search for (e.g., 'kill', 'java', 'orwell')") String keyword) {
        log.info("🔍 AI is searching for keyword: {}", keyword);

        // Use your service to filter in the DATABASE (the right way)
        List<BookAiView> results = bookService.searchByKeyword(keyword);

        if (results.isEmpty()) {
            return "I couldn't find any books matching '" + keyword + "'.";
        }

        return "I found " + results.size() + " books matching '" + keyword + "': " +
                results.stream()
                        .map(b -> b.title() + " by " + b.author())
                        .collect(Collectors.joining(", "));
    }

    @Tool(description = "Get a list of books")
    public String listBooks(int page, int size) {
        log.info("🔧 Tool Executing...");
        var books = bookService.getBookList(PageRequest.of(page, size)).getContent();

        if (books.isEmpty()) return "There are no books in the library.";

        // Just a very simple comma-separated string
        return "The books found are: " +
                books.stream()
                        .map(b -> b.title() + " by " + b.author())
                        .collect(Collectors.joining(", "));
    }

    @Tool(description = "Get a specific book by its numeric ID")
    public BookAiView getBookById(int bookId) {
        log.info("🔧 AI calling getBookById: id={}", bookId);
        return bookService.findBookById(bookId)
                .map(dto -> new BookAiView(dto.getId(), dto.getTitle(), dto.getAuthor(), dto.getIsAvailable()))
                .orElse(null);
    }
}