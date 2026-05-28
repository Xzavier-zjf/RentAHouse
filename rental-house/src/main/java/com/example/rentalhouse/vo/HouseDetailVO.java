package com.example.rentalhouse.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseDetailVO {
    private Long id;
    private String title;
    private String description;
    private String address;
    private BigDecimal price;
    private Float area;
    private Integer roomNum;
    private Integer toiletNum;
    private String orientation;
    private String decoration;
    private String facilities;
    private List<String> images;
}

