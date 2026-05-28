package com.example.rentalorder.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rentalorder.dto.HouseOrderInfo;
import com.example.rentalorder.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    @Select("""
            SELECT owner_id AS ownerId, price
            FROM house
            WHERE id = #{houseId} AND status = 1
            LIMIT 1
            """)
    HouseOrderInfo selectHouseOrderInfo(Long houseId);
}

