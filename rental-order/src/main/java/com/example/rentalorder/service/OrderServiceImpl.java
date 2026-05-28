package com.example.rentalorder.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.exception.BusinessException;
import com.example.rentalcommon.util.PaginationUtil;
import com.example.rentalorder.client.MessageClient;
import com.example.rentalorder.config.RabbitMQConfig;
import com.example.rentalorder.dto.CreateOrderRequest;
import com.example.rentalorder.dto.HouseOrderInfo;
import com.example.rentalorder.dto.OrderQueryDTO;
import com.example.rentalorder.entity.Order;
import com.example.rentalorder.mapper.OrderMapper;
import com.example.rentalorder.vo.OrderDetailVO;
import com.example.rentalorder.vo.OrderSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private MessageClient messageClient;

    @Override
    public String createOrder(Long userId, CreateOrderRequest request) {
        if (request.getStartDate() == null || request.getEndDate() == null ||
                !request.getEndDate().isAfter(request.getStartDate())) {
            throw new BusinessException("租期日期不正确");
        }

        HouseOrderInfo house = orderMapper.selectHouseOrderInfo(request.getHouseId());
        if (house == null) {
            throw new BusinessException("房源不存在或已下架");
        }

        long monthCount = ChronoUnit.MONTHS.between(request.getStartDate(), request.getEndDate());
        int months = Math.max(1, Math.toIntExact(monthCount));
        BigDecimal price = house.getPrice();

        Order order = new Order();
        order.setUserId(userId);
        order.setOwnerId(house.getOwnerId());
        order.setHouseId(request.getHouseId());
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setMonths(months);
        order.setPrice(price);
        order.setTotalAmount(price.multiply(BigDecimal.valueOf(months)));
        order.setDeposit(request.getDeposit());
        order.setStatus(0);
        order.setOrderNo("ORD" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDER_DELAY_EXCHANGE,
                RabbitMQConfig.ORDER_DELAY_ROUTING_KEY,
                order.getOrderNo()
        );

        return order.getOrderNo();
    }

    private void updateOrderStatus(Long orderId, Integer newStatus) {
        orderMapper.update(null,
                new LambdaUpdateWrapper<Order>()
                        .eq(Order::getId, orderId)
                        .set(Order::getStatus, newStatus)
                        .set(Order::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public void updateOwnerOrderStatus(Long ownerId, Long orderId, Integer newStatus) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !ownerId.equals(order.getOwnerId())) {
            throw new BusinessException("无权限或订单不存在");
        }
        if (newStatus == null || (newStatus != 2 && newStatus != 3)) {
            throw new BusinessException("订单状态不允许修改");
        }
        updateOrderStatus(orderId, newStatus);
    }

    private String generateContract(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        return """
        租房合同编号：%s
        房源ID：%d
        租客ID：%d
        房东ID：%d
        租期：%s 至 %s，共 %d 月
        月租金：%s 元
        押金：%s 元
        总金额：%s 元
        创建时间：%s

        甲乙双方应共同遵守本合同的条款与责任。
        """.formatted(
                order.getOrderNo(),
                order.getHouseId(),
                order.getUserId(),
                order.getOwnerId(),
                order.getStartDate(),
                order.getEndDate(),
                order.getMonths(),
                order.getPrice(),
                order.getDeposit(),
                order.getTotalAmount(),
                order.getCreateTime()
        );
    }

    @Override
    public String generateContract(Long currentUserId, Long orderId) {
        assertOrderAccess(currentUserId, orderId);
        return generateContract(orderId);
    }

    private void payOrder(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        if (order.getStatus() != 0) {
            throw new BusinessException("订单状态不正确，无法支付");
        }

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, orderId)
                .set(Order::getStatus, 1)
                .set(Order::getUpdateTime, LocalDateTime.now()));
    }

    @Override
    public void payOrder(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !userId.equals(order.getUserId())) {
            throw new BusinessException("无权限或订单不存在");
        }
        payOrder(orderId);
    }

    private OrderDetailVO getOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        OrderDetailVO vo = new OrderDetailVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public OrderDetailVO getOrderDetail(Long currentUserId, Long orderId) {
        assertOrderAccess(currentUserId, orderId);
        return getOrderDetail(orderId);
    }

    @Override
    public IPage<OrderSimpleVO> listMyOrders(Long userId, OrderQueryDTO dto) {
        Page<Order> page = new Page<>(PaginationUtil.normalizePage(dto.getPage()), PaginationUtil.normalizeSize(dto.getSize()));
        Page<Order> result = orderMapper.selectPage(page,
                new LambdaQueryWrapper<Order>().eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime));

        return result.convert(this::toSimpleVO);
    }

    @Override
    public IPage<OrderSimpleVO> listOwnerOrders(Long ownerId, OrderQueryDTO dto) {
        Page<Order> page = new Page<>(PaginationUtil.normalizePage(dto.getPage()), PaginationUtil.normalizeSize(dto.getSize()));
        Page<Order> result = orderMapper.selectPage(page,
                new LambdaQueryWrapper<Order>().eq(Order::getOwnerId, ownerId).orderByDesc(Order::getCreateTime));

        return result.convert(this::toSimpleVO);
    }

    private OrderSimpleVO toSimpleVO(Order order) {
        OrderSimpleVO vo = new OrderSimpleVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    private void assertOrderAccess(Long userId, Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        boolean allowed = userId.equals(order.getOwnerId()) || userId.equals(order.getUserId());
        if (!allowed) {
            throw new BusinessException("无权限访问该订单");
        }
    }

    @Override
    public void cancelOrderByOrderNo(String orderNo) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo));
        if (order == null || order.getStatus() != 0) return;

        orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                .eq(Order::getId, order.getId())
                .set(Order::getStatus, 3)
                .set(Order::getUpdateTime, LocalDateTime.now()));

        log.info("订单 {} 已被延迟机制自动取消", order.getOrderNo());

        messageClient.sendSystemMessage(order.getUserId(), "订单【" + order.getOrderNo() + "】已超时取消");
    }
}
