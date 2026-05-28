package com.example.rentalmessage.service;

import com.example.rentalmessage.entity.Message;
import com.example.rentalmessage.vo.MessageVO;

import java.util.List;

public interface MessageService {
    void sendSystemMessage(Long toUserId, String content);
    void sendChatMessage(Long fromUserId, Long toUserId, String content);
    List<Message> getChatHistory(Long userId, Long peerId);
    Long getUnreadCount(Long userId);
    void markRead(Long userId, Long peerId);

    List<MessageVO> getAllMessages(Long userId);

}

