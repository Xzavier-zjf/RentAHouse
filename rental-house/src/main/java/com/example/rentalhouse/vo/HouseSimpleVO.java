package com.example.rentalhouse.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class HouseSimpleVO {
    private Long id;
    private String title;
    private BigDecimal price;
    private String address;
    private String coverUrl;
}
