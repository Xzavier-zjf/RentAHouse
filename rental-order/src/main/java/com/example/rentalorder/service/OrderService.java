package com.example.rentalorder.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.rentalorder.dto.CreateOrderRequest;
import com.example.rentalorder.dto.OrderQueryDTO;
import com.example.rentalorder.vo.OrderDetailVO;
import com.example.rentalorder.vo.OrderSimpleVO;

public interface OrderService {
    String createOrder(Long userId, CreateOrderRequest request);
    void updateOwnerOrderStatus(Long ownerId, Long orderId, Integer newStatus);
    String generateContract(Long currentUserId, Long orderId);
    void payOrder(Long userId, Long orderId);
    OrderDetailVO getOrderDetail(Long currentUserId, Long orderId);
    IPage<OrderSimpleVO> listMyOrders(Long userId, OrderQueryDTO dto);
    IPage<OrderSimpleVO> listOwnerOrders(Long ownerId, OrderQueryDTO dto);
    void cancelOrderByOrderNo(String orderNo);
}
