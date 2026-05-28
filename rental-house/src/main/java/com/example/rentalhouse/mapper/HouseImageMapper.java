package com.example.rentalhouse.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.rentalhouse.entity.HouseImage;
import com.example.rentalhouse.vo.HouseSimpleVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface HouseImageMapper extends BaseMapper<HouseImage> {
    @Select("SELECT url FROM house_image WHERE house_id = #{houseId} ORDER BY sort")
    List<String> selectImageUrls(Long houseId);

    @Select("""
    SELECT h.id, h.title, h.price, h.address,
           (SELECT url FROM house_image i WHERE i.house_id = h.id AND is_cover = 1 LIMIT 1) AS coverUrl
    FROM house h
    WHERE h.status = 1
    ORDER BY h.view_count DESC
    LIMIT #{limit}
""")
    List<HouseSimpleVO> selectHotHouses(@Param("limit") int limit);

}


