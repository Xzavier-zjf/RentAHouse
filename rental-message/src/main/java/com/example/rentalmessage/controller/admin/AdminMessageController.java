package com.example.rentalmessage.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.util.PaginationUtil;
import com.example.rentalcommon.util.SecurityUtil;
import com.example.rentalmessage.entity.Message;
import com.example.rentalmessage.mapper.MessageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/message")
@RequiredArgsConstructor
public class AdminMessageController {

    private final MessageMapper messageMapper;

    @GetMapping("/list")
    public ResponseEntity<IPage<Message>> list(@RequestParam(defaultValue = "1") int page,
                                               @RequestParam(defaultValue = "20") int size) {
        SecurityUtil.requireRole("admin");
        return ResponseEntity.ok(messageMapper.selectPage(
                new Page<>(PaginationUtil.normalizePage(page), PaginationUtil.normalizeSize(size)),
                new LambdaQueryWrapper<Message>().orderByDesc(Message::getCreateTime)));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> messageStats() {
        SecurityUtil.requireRole("admin");
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", messageMapper.selectCount(null));
        stats.put("unread", messageMapper.selectCount(new LambdaQueryWrapper<Message>().eq(Message::getIsRead, 0)));
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/filter")
    public ResponseEntity<?> filterMessageContent(@RequestBody String content) {
        SecurityUtil.requireRole("admin");
        if (content.contains("敏感词")) {
            return ResponseEntity.badRequest().body("消息内容不合法");
        }
        return ResponseEntity.ok("内容通过");
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filter(@RequestParam String keyword) {
        SecurityUtil.requireRole("admin");
        return ResponseEntity.ok(
                messageMapper.selectList(new LambdaQueryWrapper<Message>().like(Message::getContent, keyword))
        );
    }
}

