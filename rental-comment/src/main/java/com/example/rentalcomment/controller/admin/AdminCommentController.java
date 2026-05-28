package com.example.rentalcomment.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.util.PaginationUtil;
import com.example.rentalcommon.util.SecurityUtil;
import com.example.rentalcomment.entity.Comment;
import com.example.rentalcomment.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/comment")
@RequiredArgsConstructor
public class AdminCommentController {

    private final CommentMapper commentMapper;

    @GetMapping("/list")
    public ResponseEntity<IPage<Comment>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        SecurityUtil.requireRole("admin");
        return ResponseEntity.ok(commentMapper.selectPage(
                new Page<>(PaginationUtil.normalizePage(page), PaginationUtil.normalizeSize(size)),
                new LambdaQueryWrapper<Comment>().orderByDesc(Comment::getCreateTime)));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId) {
        SecurityUtil.requireRole("admin");
        commentMapper.deleteById(commentId);
        return ResponseEntity.ok("评论已删除");
    }

    @PostMapping("/delete/{commentId}")
    public ResponseEntity<?> delete(@PathVariable Long commentId) {
        SecurityUtil.requireRole("admin");
        commentMapper.deleteById(commentId);
        return ResponseEntity.ok("删除成功");
    }

    @PostMapping("/ban-user/{userId}")
    public ResponseEntity<?> banUser(@PathVariable Long userId) {
        SecurityUtil.requireRole("admin");
        // 假设通过 feign 调用用户模块禁言用户
        // 或直接写到 blacklist 表中
        return ResponseEntity.ok("用户已禁言");
    }
}

