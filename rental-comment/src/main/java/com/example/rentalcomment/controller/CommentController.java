package com.example.rentalcomment.controller;


import com.example.rentalcomment.dto.AddCommentRequest;
import com.example.rentalcomment.service.CommentService;
import com.example.rentalcomment.vo.CommentVO;
import com.example.rentalcommon.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论接口")
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "添加评论")
    @PostMapping("/add")
    public ResponseEntity<?> add(@RequestBody AddCommentRequest request) {
        SecurityUtil.requireRole("user");
        Long userId = SecurityUtil.getCurrentUserId();
        commentService.addComment(userId, request);
        return ResponseEntity.ok("评论成功");
    }

    @Operation(summary = "获取房源评论")
    @GetMapping("/list/{houseId}")
    public ResponseEntity<List<CommentVO>> list(@PathVariable Long houseId) {
        return ResponseEntity.ok(commentService.getCommentsByHouseId(houseId));
    }

    @Operation(summary = "点赞评论")
    @PostMapping("/like/{id}")
    public ResponseEntity<?> like(@PathVariable Long id) {
        SecurityUtil.requireRole("user");
        commentService.like(id);
        return ResponseEntity.ok("点赞成功");
    }
}
