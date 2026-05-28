package com.example.rentalorder.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.util.PaginationUtil;
import com.example.rentalcommon.util.SecurityUtil;
import com.example.rentalorder.entity.Order;
import com.example.rentalorder.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderMapper orderMapper;

    @GetMapping("/list")
    public ResponseEntity<IPage<Order>> list(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "20") int size) {
        SecurityUtil.requireRole("admin");
        return ResponseEntity.ok(orderMapper.selectPage(
                new Page<>(PaginationUtil.normalizePage(page), PaginationUtil.normalizeSize(size)),
                new LambdaQueryWrapper<Order>().orderByDesc(Order::getCreateTime)));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> orderStats() {
        SecurityUtil.requireRole("admin");
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", orderMapper.selectCount(null));
        stats.put("paid", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 1)));
        stats.put("cancelled", orderMapper.selectCount(new LambdaQueryWrapper<Order>().eq(Order::getStatus, 3)));
        return ResponseEntity.ok(stats);
    }
}
