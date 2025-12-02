package com.daniellaera.backend.dao;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor // Fixes the deserialization issue
@AllArgsConstructor // Ensures the @Builder annotation still works correctly
public class BorrowDTO {
    private Integer bookId;
    private Integer userId;

    @NotNull(message = "Borrow start date is required")
    private Date borrowStartDate;

    @FutureOrPresent(message = "Borrow end date must be in the future or present")
    private Date borrowEndDate;
    private Boolean isReturned;
}
