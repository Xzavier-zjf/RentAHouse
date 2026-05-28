package com.example.rentalhouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("house")
public class House {
    private Long id;
    private String title;
    private String description;
    private Long ownerId;
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
    private Integer status;
    private Integer viewCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private Integer auditStatus; // Lombok 自动生成 getter/setter
}
