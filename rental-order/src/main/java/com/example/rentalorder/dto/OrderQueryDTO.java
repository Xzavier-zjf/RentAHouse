package com.example.rentalorder.dto;

import lombok.Data;

@Data
public class OrderQueryDTO {
    private int page = 1;
    private int size = 10;
}

