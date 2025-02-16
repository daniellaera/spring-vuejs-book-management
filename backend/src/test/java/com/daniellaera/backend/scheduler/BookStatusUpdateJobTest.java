package com.daniellaera.backend.scheduler;

import com.daniellaera.backend.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BookStatusUpdateJobTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookStatusUpdateJob bookStatusUpdateJob;

    @Mock
    private JobExecutionContext jobExecutionContext;

    @Test
    void testExecute_UpdatesBookStatus() throws JobExecutionException {
        bookStatusUpdateJob.execute(jobExecutionContext);

        verify(bookService, times(1)).updateExpiredBookStatus();
    }

    @Test
    void testExecute_ThrowsJobExecutionException_WhenExceptionOccurs() {
        doThrow(new RuntimeException("Error occurred")).when(bookService).updateExpiredBookStatus();

        try {
            bookStatusUpdateJob.execute(jobExecutionContext);
        } catch (JobExecutionException e) {
            assert(e.getMessage().contains("Failed to update book statuses"));
        }
    }
}
