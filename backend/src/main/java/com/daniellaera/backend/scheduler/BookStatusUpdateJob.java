package com.daniellaera.backend.scheduler;

import com.daniellaera.backend.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class BookStatusUpdateJob implements Job {

    private final BookService bookService;

    @Autowired
    public BookStatusUpdateJob(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        log.info("Running BookStatusUpdateJob...");
        try {
            bookService.updateExpiredBookStatus();
        } catch (Exception e) {
            log.error("Error occurred while updating book statuses: {}", e.getMessage(), e);
            throw new JobExecutionException("Failed to update book statuses", e);
        }
    }
}
