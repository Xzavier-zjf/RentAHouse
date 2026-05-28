package com.example.rentalmessage.controller;

import com.example.rentalcommon.util.SecurityUtil;
import com.example.rentalmessage.dto.ChatRequest;
import com.example.rentalmessage.dto.MarkReadRequest;
import com.example.rentalmessage.entity.Message;
import com.example.rentalmessage.service.MessageService;
import com.example.rentalmessage.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @PostMapping("/system")
    public ResponseEntity<String> sendSystemMessage(@RequestParam Long toUserId,
                                                    @RequestParam String content) {
        SecurityUtil.requireRole("admin");
        messageService.sendSystemMessage(toUserId, content);
        return ResponseEntity.ok("系统消息发送成功");
    }

    // 发送聊天消息
    @PostMapping("/chat")
    public ResponseEntity<String> sendChat(@RequestBody ChatRequest request) {
        SecurityUtil.requireAnyRole("user", "owner");
        Long fromUserId = SecurityUtil.getCurrentUserId();
        messageService.sendChatMessage(fromUserId, request.getToUserId(), request.getContent());
        return ResponseEntity.ok("消息发送成功");
    }

    // 获取与某人的聊天记录
    @GetMapping("/chat/{peerId}")
    public ResponseEntity<List<Message>> chatHistory(@PathVariable Long peerId) {
        SecurityUtil.requireAnyRole("user", "owner");
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(messageService.getChatHistory(userId, peerId));
    }

    // 标记与某人的消息为已读
    @PostMapping("/read/{peerId}")
    public ResponseEntity<String> markRead(@PathVariable Long peerId) {
        SecurityUtil.requireAnyRole("user", "owner");
        Long userId = SecurityUtil.getCurrentUserId();
        messageService.markRead(userId, peerId);
        return ResponseEntity.ok("已标记为已读");
    }

    // 获取当前用户所有未读消息数
    @GetMapping("/unread-count")
    public ResponseEntity<Long> unreadCount() {
        SecurityUtil.requireAnyRole("user", "owner", "admin");
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(messageService.getUnreadCount(userId));
    }

    // 获取当前用户收到的所有消息
    @GetMapping("/list")
    public ResponseEntity<List<MessageVO>> listMyMessages() {
        SecurityUtil.requireAnyRole("user", "owner", "admin");
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(messageService.getAllMessages(userId));
    }
}
