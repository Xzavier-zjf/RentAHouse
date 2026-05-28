package com.example.rentalorder.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.rentalorder.dto.CreateOrderRequest;
import com.example.rentalorder.dto.OrderQueryDTO;
import com.example.rentalorder.service.OrderService;
import com.example.rentalorder.vo.OrderDetailVO;
import com.example.rentalorder.vo.OrderSimpleVO;
import com.example.rentalcommon.util.SecurityUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody CreateOrderRequest request) {
        SecurityUtil.requireRole("user");
        Long userId = SecurityUtil.getCurrentUserId();
        String orderNo = orderService.createOrder(userId, request);
        return ResponseEntity.ok(orderNo);
    }

    @PostMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        SecurityUtil.requireRole("owner");
        Long ownerId = SecurityUtil.getCurrentUserId();
        orderService.updateOwnerOrderStatus(ownerId, id, status);
        return ResponseEntity.ok("订单状态更新成功");
    }

    @GetMapping("/contract/{id}")
    public ResponseEntity<String> contract(@PathVariable Long id) {
        SecurityUtil.requireAnyRole("user", "owner");
        return ResponseEntity.ok(orderService.generateContract(SecurityUtil.getCurrentUserId(), id));
    }

    @GetMapping("/pay-page/{orderId}")
    public void simulateAlipay(@PathVariable Long orderId, HttpServletResponse response) throws IOException {
        SecurityUtil.requireRole("user");
        orderService.getOrderDetail(SecurityUtil.getCurrentUserId(), orderId);
        // 模拟跳转到支付页
        response.setContentType("text/html;charset=utf-8");
        response.getWriter().write("""
        <html><body>
        <h3>模拟支付</h3>
        <form method='post' action='/api/order/pay/%d'>
            <input type='submit' value='确认支付'>
        </form>
        </body></html>
    """.formatted(orderId));
    }

    @PostMapping("/pay/{orderId}")
    public ResponseEntity<String> pay(@PathVariable Long orderId) {
        SecurityUtil.requireRole("user");
        orderService.payOrder(SecurityUtil.getCurrentUserId(), orderId);
        return ResponseEntity.ok("模拟支付成功，订单已更新状态");
    }


    @GetMapping("/{id}")
    public ResponseEntity<OrderDetailVO> detail(@PathVariable Long id) {
        SecurityUtil.requireAnyRole("user", "owner");
        return ResponseEntity.ok(orderService.getOrderDetail(SecurityUtil.getCurrentUserId(), id));
    }

    @PostMapping("/my")
    public ResponseEntity<IPage<OrderSimpleVO>> myOrders(@RequestBody OrderQueryDTO dto) {
        SecurityUtil.requireRole("user");
        Long userId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(orderService.listMyOrders(userId, dto));
    }

    @PostMapping("/owner")
    public ResponseEntity<IPage<OrderSimpleVO>> ownerOrders(@RequestBody OrderQueryDTO dto) {
        SecurityUtil.requireRole("owner");
        Long ownerId = SecurityUtil.getCurrentUserId();
        return ResponseEntity.ok(orderService.listOwnerOrders(ownerId, dto));
    }



}
