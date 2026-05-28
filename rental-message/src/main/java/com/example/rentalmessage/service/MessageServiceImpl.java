package com.example.rentalmessage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.rentalmessage.entity.Message;
import com.example.rentalmessage.mapper.MessageMapper;
import com.example.rentalmessage.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;

    @Override
    public void sendSystemMessage(Long toUserId, String content) {
        Message m = new Message();
        m.setFromUserId(0L); // 系统用户
        m.setToUserId(toUserId);
        m.setContent(content);
        m.setIsRead(0);
        m.setType(1);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);
    }

    @Override
    public void sendChatMessage(Long fromUserId, Long toUserId, String content) {
        Message m = new Message();
        m.setFromUserId(fromUserId);
        m.setToUserId(toUserId);
        m.setContent(content);
        m.setIsRead(0);
        m.setType(2);
        m.setCreateTime(LocalDateTime.now());
        messageMapper.insert(m);
    }

    @Override
    public List<Message> getChatHistory(Long userId, Long peerId) {
        return messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .and(q -> q
                        .eq(Message::getFromUserId, userId).eq(Message::getToUserId, peerId)
                        .or()
                        .eq(Message::getFromUserId, peerId).eq(Message::getToUserId, userId))
                .eq(Message::getType, 2)
                .orderByAsc(Message::getCreateTime));
    }

    @Override
    public Long getUnreadCount(Long userId) {
        return messageMapper.selectCount(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .eq(Message::getIsRead, 0)
        );
    }



    @Override
    public void markRead(Long userId, Long peerId) {
        messageMapper.update(null, new LambdaUpdateWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getFromUserId, peerId)
                .eq(Message::getType, 2)
                .set(Message::getIsRead, 1));
    }

    @Override
    public List<MessageVO> getAllMessages(Long userId) {
        List<Message> list = messageMapper.selectList(
                new LambdaQueryWrapper<Message>()
                        .eq(Message::getToUserId, userId)
                        .orderByDesc(Message::getCreateTime)
        );

        return list.stream().map(m -> {
            MessageVO vo = new MessageVO();
            BeanUtils.copyProperties(m, vo);
            return vo;
        }).toList();
    }




}
