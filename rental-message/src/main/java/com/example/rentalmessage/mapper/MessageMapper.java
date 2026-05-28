package com.example.rentalmessage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rentalmessage.entity.Message;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
