package com.example.rentalcomment.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {
    private Long id;
    private Long userId;
    private String content;
    private Integer rating;
    private Integer likeCount;
    private LocalDateTime createTime;
    private List<String> imageUrls;
    private List<CommentVO> replies;

    private String nickname;
    private String avatar;
}

