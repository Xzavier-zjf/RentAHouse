package com.example.rentalhouse.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("house_favorite")
public class HouseFavorite {
    private Long id;
    private Long userId;
    private Long houseId;
    private LocalDateTime createTime;
}
