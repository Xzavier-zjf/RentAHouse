package com.example.rentalmessage.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private Long toUserId;
    private String content;
}
