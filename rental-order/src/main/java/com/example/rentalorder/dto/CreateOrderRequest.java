package com.example.rentalorder.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

@Data
public class CreateOrderRequest {
    private Long houseId;
    private LocalDate startDate;  // ✅ LocalDate 而不是 java.util.Date
    private LocalDate endDate;
    private BigDecimal deposit;
}

