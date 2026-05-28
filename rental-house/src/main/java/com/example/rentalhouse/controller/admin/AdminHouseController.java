package com.example.rentalhouse.controller.admin;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.util.PaginationUtil;
import com.example.rentalcommon.util.SecurityUtil;
import com.example.rentalhouse.entity.House;
import com.example.rentalhouse.mapper.HouseMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/house")
@RequiredArgsConstructor
public class AdminHouseController {

    private final HouseMapper houseMapper;

    @GetMapping("/list")
    public ResponseEntity<IPage<House>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        SecurityUtil.requireRole("admin");
        return ResponseEntity.ok(houseMapper.selectPage(
                new Page<>(PaginationUtil.normalizePage(page), PaginationUtil.normalizeSize(size)),
                null));
    }

    @PostMapping("/status/{houseId}")
    public ResponseEntity<?> updateStatus(@PathVariable Long houseId, @RequestParam int status) {
        SecurityUtil.requireRole("admin");
        houseMapper.update(null, new LambdaUpdateWrapper<House>()
                .eq(House::getId, houseId)
                .set(House::getStatus, status));
        return ResponseEntity.ok("状态修改成功");
    }

    @PostMapping("/audit/{houseId}")
    public ResponseEntity<?> auditHouse(@PathVariable Long houseId, @RequestParam boolean approved) {
        SecurityUtil.requireRole("admin");
        houseMapper.update(null, new LambdaUpdateWrapper<House>()
                .eq(House::getId, houseId)
                .set(House::getAuditStatus, approved ? 1 : 2)
                .set(House::getStatus, approved ? 1 : 0)); // 1通过 2拒绝
        return ResponseEntity.ok("审核完成");
    }
}
