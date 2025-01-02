package com.daniellaera.backend.dao;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class BorrowDTO {
    private Integer bookId;
    private Integer userId;
    private Date borrowStartDate;
    private Date borrowEndDate;
    private Boolean isReturned;
}
