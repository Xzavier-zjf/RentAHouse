package com.example.rentalcomment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rentalcomment.entity.CommentImage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentImageMapper extends BaseMapper<CommentImage> {

    @Select("SELECT url FROM comment_image WHERE comment_id = #{commentId}")
    List<String> findImageUrls(@Param("commentId") Long commentId);
}

