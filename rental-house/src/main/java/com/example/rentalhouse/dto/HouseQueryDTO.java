package com.example.rentalhouse.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HouseQueryDTO {
    private String city;
    private String district;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Float minArea;
    private Float maxArea;
    private Integer page = 1;
    private Integer size = 10;
    private String keyword;
}

