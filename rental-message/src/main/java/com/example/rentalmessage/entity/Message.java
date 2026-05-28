package com.example.rentalmessage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("message")
public class Message {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private String content;
    private Integer isRead;
    private Integer type; // 1=系统 2=用户
    private LocalDateTime createTime;
}

