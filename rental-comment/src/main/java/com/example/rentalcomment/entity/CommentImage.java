package com.example.rentalcomment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("comment_image")
public class CommentImage {
    private Long id;
    private Long commentId;
    private String url;
    private LocalDateTime createTime;
}

