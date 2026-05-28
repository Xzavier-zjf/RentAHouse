package com.example.rentalcomment.dto;

import lombok.Data;

import java.util.List;

@Data
public class AddCommentRequest {
    private Long houseId;
    private String content;
    private Integer rating;
    private List<String> imageUrls;
    private Long parentId; // 回复评论时填写
}
