package com.example.rentalorder.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HouseOrderInfo {
    private Long ownerId;
    private BigDecimal price;
}
