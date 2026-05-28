package com.example.rentalcomment.service;

import com.example.rentalcomment.dto.AddCommentRequest;
import com.example.rentalcomment.vo.CommentVO;

import java.util.List;

public interface CommentService {
    void addComment(Long userId, AddCommentRequest request);
    List<CommentVO> getCommentsByHouseId(Long houseId);
    void like(Long commentId);
}

