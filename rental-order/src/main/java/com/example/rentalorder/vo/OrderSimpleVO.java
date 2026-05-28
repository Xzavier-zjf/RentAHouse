package com.example.rentalorder.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class OrderSimpleVO {
    private Long id;
    private String orderNo;
    private Long houseId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalAmount;
    private Integer status;
    private LocalDateTime createTime;
}
