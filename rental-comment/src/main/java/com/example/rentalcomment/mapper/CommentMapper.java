package com.example.rentalcomment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rentalcomment.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}

