package com.example.rentalorder.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("`order`")
public class Order {
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
