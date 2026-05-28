package com.example.rentalorder.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderDetailVO {
    private Long id;
    private String orderNo;
    private Long houseId;
    private Long userId;
    private Long ownerId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer months;
    private BigDecimal price;
    private BigDecimal totalAmount;
    private BigDecimal deposit;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

