package com.example.rentalhouse.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class HouseUploadDTO {
    private String title;
    private String description;
    private String address;
    private String city;
    private String district;
    private BigDecimal price;
    private Float area;
    private Integer roomNum;
    private Integer toiletNum;
    private Integer floor;
    private Integer totalFloor;
    private String orientation;
    private String decoration;
    private String facilities;
    private List<String> imageUrls;  // 图片 URL（前端先上传图片，返回链接）
}

