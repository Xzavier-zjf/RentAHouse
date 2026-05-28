package com.example.rentalhouse.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.util.PaginationUtil;
import com.example.rentalhouse.dto.HouseQueryDTO;
import com.example.rentalhouse.dto.HouseUploadDTO;
import com.example.rentalhouse.entity.House;
import com.example.rentalhouse.entity.HouseFavorite;
import com.example.rentalhouse.entity.HouseImage;
import com.example.rentalcommon.exception.BusinessException;
import com.example.rentalhouse.mapper.HouseImageMapper;
import com.example.rentalhouse.mapper.HouseMapper;
import com.example.rentalhouse.vo.HouseDetailVO;
import com.example.rentalhouse.vo.HouseSimpleVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class HouseServiceImpl implements HouseService {

    @Autowired private HouseMapper houseMapper;

    @Autowired private HouseImageMapper imageMapper;

    @Autowired
    private HouseMapper.HouseFavoriteMapper favoriteMapper;

    @Override
    public List<HouseSimpleVO> getHotHouses(int limit) {
        return houseMapper.selectHotHouses(Math.min(Math.max(limit, 1), PaginationUtil.MAX_SIZE));
    }

    @Override
    public void uploadHouse(Long ownerId, HouseUploadDTO dto) {
        House house = new House();
        BeanUtils.copyProperties(dto, house);
        house.setOwnerId(ownerId);
        house.setStatus(0);
        house.setAuditStatus(0);
        house.setCreateTime(LocalDateTime.now());
        houseMapper.insert(house);

        // 保存图片
        if (dto.getImageUrls() != null) {
            int sort = 0;
            for (String url : dto.getImageUrls()) {
                HouseImage img = new HouseImage();
                img.setHouseId(house.getId());
                img.setUrl(url);
                img.setIsCover(sort == 0 ? 1 : 0); // 第一张为封面
                img.setSort(sort++);
                img.setCreateTime(LocalDateTime.now());
                imageMapper.insert(img);
            }
        }
    }

    @Override
    public HouseDetailVO getHouseDetail(Long houseId) {
        House house = houseMapper.selectById(houseId);
        if (house == null || house.getStatus() == 0) {
            throw new BusinessException("房源不存在或已下架");
        }

        HouseDetailVO vo = new HouseDetailVO();
        BeanUtils.copyProperties(house, vo);

        // 房源图片
        vo.setImages(imageMapper.selectImageUrls(houseId));

        // 增加浏览量
        houseMapper.update(null, new LambdaUpdateWrapper<House>()
                .eq(House::getId, houseId)
                .setSql("view_count = view_count + 1"));

        return vo;
    }


    @Override
    public IPage<House> searchHouses(HouseQueryDTO dto) {
        Page<House> page = new Page<>(PaginationUtil.normalizePage(dto.getPage()), PaginationUtil.normalizeSize(dto.getSize()));
        LambdaQueryWrapper<House> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(StringUtils.hasText(dto.getCity()), House::getCity, dto.getCity())
                .eq(StringUtils.hasText(dto.getDistrict()), House::getDistrict, dto.getDistrict())
                .ge(dto.getMinPrice() != null, House::getPrice, dto.getMinPrice())
                .le(dto.getMaxPrice() != null, House::getPrice, dto.getMaxPrice())
                .ge(dto.getMinArea() != null, House::getArea, dto.getMinArea())
                .le(dto.getMaxArea() != null, House::getArea, dto.getMaxArea())
                .like(StringUtils.hasText(dto.getKeyword()), House::getTitle, dto.getKeyword());

        wrapper.eq(House::getStatus, 1);
        wrapper.eq(House::getAuditStatus, 1);
        return houseMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<House> listOwnerHouses(Long ownerId, int page, int size) {
        Page<House> pg = new Page<>(PaginationUtil.normalizePage(page), PaginationUtil.normalizeSize(size));
        return houseMapper.selectPage(pg, new LambdaQueryWrapper<House>()
                .eq(House::getOwnerId, ownerId)
                .orderByDesc(House::getCreateTime));
    }

    @Override
    public IPage<House> listFavoriteHouses(Long userId, int page, int size) {
        int safePage = PaginationUtil.normalizePage(page);
        int safeSize = PaginationUtil.normalizeSize(size);
        Page<HouseFavorite> favoritePage = new Page<>(safePage, safeSize);
        Page<HouseFavorite> favorites = favoriteMapper.selectPage(favoritePage,
                new LambdaQueryWrapper<HouseFavorite>()
                        .eq(HouseFavorite::getUserId, userId)
                        .orderByDesc(HouseFavorite::getCreateTime));

        Page<House> result = new Page<>(safePage, safeSize);
        result.setTotal(favorites.getTotal());
        result.setPages(favorites.getPages());
        result.setCurrent(favorites.getCurrent());
        result.setSize(favorites.getSize());

        List<Long> houseIds = favorites.getRecords().stream()
                .map(HouseFavorite::getHouseId)
                .toList();
        if (houseIds.isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }

        result.setRecords(houseMapper.selectList(new LambdaQueryWrapper<House>()
                .in(House::getId, houseIds)
                .eq(House::getStatus, 1)
                .eq(House::getAuditStatus, 1)));
        return result;
    }


    @Override
    public void changeStatus(Long houseId, Long userId, Integer status) {
        House house = houseMapper.selectById(houseId);
        if (house == null || !house.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限或房源不存在");
        }

        house.setStatus(status);
        house.setUpdateTime(LocalDateTime.now());
        houseMapper.updateById(house);
    }

    @Override
    public void toggleFavorite(Long userId, Long houseId) {
        LambdaQueryWrapper<HouseFavorite> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HouseFavorite::getUserId, userId).eq(HouseFavorite::getHouseId, houseId);
        HouseFavorite favorite = favoriteMapper.selectOne(wrapper);
        if (favorite == null) {
            favorite = new HouseFavorite();
            favorite.setUserId(userId);
            favorite.setHouseId(houseId);
            favorite.setCreateTime(LocalDateTime.now());
            favoriteMapper.insert(favorite);
        } else {
            favoriteMapper.deleteById(favorite.getId());
        }
    }

    @Override
    public boolean isFavorite(Long userId, Long houseId) {
        return favoriteMapper.selectCount(new LambdaQueryWrapper<HouseFavorite>()
                .eq(HouseFavorite::getUserId, userId)
                .eq(HouseFavorite::getHouseId, houseId)) > 0;
    }

    @Override
    public void updateHouse(Long houseId, Long userId, HouseUploadDTO dto) {
        House house = houseMapper.selectById(houseId);
        if (house == null || !house.getOwnerId().equals(userId)) {
            throw new BusinessException("无权限或房源不存在");
        }

        BeanUtils.copyProperties(dto, house);
        house.setStatus(0);
        house.setAuditStatus(0);
        house.setUpdateTime(LocalDateTime.now());
        houseMapper.updateById(house);

        // 更新图片：先删旧，再加新
        imageMapper.delete(new LambdaQueryWrapper<HouseImage>().eq(HouseImage::getHouseId, houseId));

        if (dto.getImageUrls() != null) {
            int sort = 0;
            for (String url : dto.getImageUrls()) {
                HouseImage img = new HouseImage();
                img.setHouseId(houseId);
                img.setUrl(url);
                img.setIsCover(sort == 0 ? 1 : 0);
                img.setSort(sort++);
                img.setCreateTime(LocalDateTime.now());
                imageMapper.insert(img);
            }
        }
    }


}

