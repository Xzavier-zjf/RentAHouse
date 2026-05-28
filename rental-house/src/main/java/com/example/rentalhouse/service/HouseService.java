package com.example.rentalhouse.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.rentalhouse.dto.HouseQueryDTO;
import com.example.rentalhouse.dto.HouseUploadDTO;
import com.example.rentalhouse.entity.House;
import com.example.rentalhouse.vo.HouseDetailVO;
import com.example.rentalhouse.vo.HouseSimpleVO;

import java.util.List;

public interface HouseService {
    IPage<House> searchHouses(HouseQueryDTO dto);
    IPage<House> listOwnerHouses(Long ownerId, int page, int size);
    IPage<House> listFavoriteHouses(Long userId, int page, int size);
    void uploadHouse(Long ownerId, HouseUploadDTO dto);
    HouseDetailVO getHouseDetail(Long houseId);
    List<HouseSimpleVO> getHotHouses(int limit);

    void changeStatus(Long houseId, Long userId, Integer status);

    void toggleFavorite(Long userId, Long houseId);
    boolean isFavorite(Long userId, Long houseId);
    void updateHouse(Long houseId, Long userId, HouseUploadDTO dto);



}


