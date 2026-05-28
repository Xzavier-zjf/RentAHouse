package com.example.rentalcomment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment")
public class Comment {
    private Long id;
    private Long houseId;
    private Long userId;
    private String content;
    private Integer rating;
    private Integer likeCount;
    private Long parentId;
    private LocalDateTime createTime;
}

