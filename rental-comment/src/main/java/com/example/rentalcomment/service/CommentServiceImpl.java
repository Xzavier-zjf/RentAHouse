package com.example.rentalcomment.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.rentalcomment.client.UserClient;
import com.example.rentalcomment.dto.AddCommentRequest;
import com.example.rentalcomment.entity.Comment;
import com.example.rentalcomment.entity.CommentImage;
import com.example.rentalcomment.mapper.CommentImageMapper;
import com.example.rentalcomment.mapper.CommentMapper;
import com.example.rentalcomment.vo.CommentVO;
import com.example.rentalcomment.vo.UserInfoVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final CommentImageMapper imageMapper;
    private final UserClient userClient;

    public CommentServiceImpl(CommentMapper commentMapper, CommentImageMapper imageMapper, UserClient userClient) {
        this.commentMapper = commentMapper;
        this.imageMapper = imageMapper;
        this.userClient = userClient;
    }

    @Override
    public void addComment(Long userId, AddCommentRequest request) {
        Comment comment = new Comment();
        BeanUtils.copyProperties(request, comment);
        comment.setUserId(userId);
        comment.setCreateTime(LocalDateTime.now());
        comment.setLikeCount(0);
        commentMapper.insert(comment);

        if (request.getImageUrls() != null) {
            for (String url : request.getImageUrls()) {
                CommentImage img = new CommentImage();
                img.setCommentId(comment.getId());
                img.setUrl(url);
                img.setCreateTime(LocalDateTime.now());
                imageMapper.insert(img);
            }
        }
    }

    @Override
    public List<CommentVO> getCommentsByHouseId(Long houseId) {
        List<Comment> roots = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getHouseId, houseId)
                .and(wrapper -> wrapper.isNull(Comment::getParentId).or().eq(Comment::getParentId, 0L))
                .orderByDesc(Comment::getCreateTime));

        List<CommentVO> result = new ArrayList<>();
        for (Comment comment : roots) {
            result.add(toVO(comment));
        }
        return result;
    }

    private CommentVO toVO(Comment c) {
        CommentVO vo = new CommentVO();
        BeanUtils.copyProperties(c, vo);
        vo.setImageUrls(imageMapper.findImageUrls(c.getId()));

        UserInfoVO user = userClient.getUser(c.getUserId());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());

        List<Comment> replies = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getParentId, c.getId()));
        vo.setReplies(replies.stream().map(this::toVO).toList());

        return vo;
    }

    @Override
    public void like(Long commentId) {
        commentMapper.update(null, new LambdaUpdateWrapper<Comment>()
                .eq(Comment::getId, commentId)
                .setSql("like_count = like_count + 1"));
    }
}
