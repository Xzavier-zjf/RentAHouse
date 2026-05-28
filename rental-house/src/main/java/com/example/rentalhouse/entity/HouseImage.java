package com.example.rentalhouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("house_image")
public class HouseImage {
    private Long id;
    private Long houseId;
    private String url;
    private Integer isCover;
    private Integer sort;
    private LocalDateTime createTime;
}
