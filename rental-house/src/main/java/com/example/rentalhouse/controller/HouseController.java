package com.example.rentalhouse.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.rentalhouse.dto.HouseQueryDTO;
import com.example.rentalhouse.dto.HouseUploadDTO;
import com.example.rentalhouse.entity.House;
import com.example.rentalhouse.service.HouseService;
import com.example.rentalhouse.service.MongoFileStorageService;
import com.example.rentalcommon.util.SecurityUtil;
import com.example.rentalhouse.vo.HouseDetailVO;
import com.example.rentalhouse.vo.HouseSimpleVO;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/house")
public class HouseController {

    @Autowired
    private HouseService houseService;

    @Autowired
    private MongoFileStorageService mongoFileStorageService;

    @PostMapping("/search")
    public ResponseEntity<IPage<House>> search(@RequestBody HouseQueryDTO dto) {
        return ResponseEntity.ok(houseService.searchHouses(dto));
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestBody HouseUploadDTO dto) {
        SecurityUtil.requireRole("owner");
        Long userId = SecurityUtil.getCurrentUserId();
        houseService.uploadHouse(userId, dto);
        return ResponseEntity.ok("发布成功");
    }

    @GetMapping("/owner/list")
    public ResponseEntity<IPage<House>> ownerHouses(@RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        SecurityUtil.requireRole("owner");
        Long ownerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(houseService.listOwnerHouses(ownerId, page, size));
    }

    @GetMapping("/{houseId}")
    public ResponseEntity<HouseDetailVO> detail(@PathVariable Long houseId) {
        return ResponseEntity.ok(houseService.getHouseDetail(houseId));
    }

    @GetMapping("/hot")
    public ResponseEntity<List<HouseSimpleVO>> hot(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(houseService.getHotHouses(limit));
    }

    @PostMapping("/change-status")
    public ResponseEntity<?> changeStatus(@RequestParam Long houseId, @RequestParam Integer status) {
        SecurityUtil.requireRole("owner");
        Long userId = SecurityUtil.getCurrentUserId();
        houseService.changeStatus(houseId, userId, status);
        return ResponseEntity.ok("修改成功");
    }

    @PostMapping("/favorite/toggle")
    public ResponseEntity<?> toggleFavorite(@RequestParam Long houseId) {
        SecurityUtil.requireRole("user");
        Long userId = SecurityUtil.getCurrentUserId();
        houseService.toggleFavorite(userId, houseId);
        return ResponseEntity.ok("操作成功");
    }

    @GetMapping("/favorite/check")
    public ResponseEntity<Boolean> checkFavorite(@RequestParam Long houseId) {
        SecurityUtil.requireRole("user");
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(houseService.isFavorite(userId, houseId));
    }

    @GetMapping("/favorite/list")
    public ResponseEntity<IPage<House>> listFavorites(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "12") int size) {
        SecurityUtil.requireRole("user");
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(houseService.listFavoriteHouses(userId, page, size));
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateHouse(@RequestParam Long houseId, @RequestBody HouseUploadDTO dto) {
        SecurityUtil.requireRole("owner");
        Long userId = SecurityUtil.getCurrentUserId();
        houseService.updateHouse(houseId, userId, dto);
        return ResponseEntity.ok("修改成功");
    }

    @PostMapping("/file/upload")
    public ResponseEntity<String> uploadHouseImage(@RequestParam("file") MultipartFile file) throws IOException {
        SecurityUtil.requireRole("owner");
        return ResponseEntity.ok(mongoFileStorageService.storeHouseImage(file));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileId) {
        GridFsResource resource = mongoFileStorageService.load(fileId);
        String contentType = resource.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : resource.getContentType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

}
