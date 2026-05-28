package com.example.rentalorder.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.rentalorder.entity.Order;
import com.example.rentalorder.mapper.OrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderScheduledTask {

    private final OrderMapper orderMapper;

    @Scheduled(fixedRate = 300000) // 每 5 分钟执行
    public void cancelExpiredOrders() {
        LocalDateTime timeout = LocalDateTime.now().minusMinutes(15);
        List<Order> expired = orderMapper.selectList(new LambdaQueryWrapper<Order>()
                .eq(Order::getStatus, 0)
                .lt(Order::getCreateTime, timeout));

        for (Order order : expired) {
            orderMapper.update(null, new LambdaUpdateWrapper<Order>()
                    .eq(Order::getId, order.getId())
                    .set(Order::getStatus, 3)
                    .set(Order::getUpdateTime, LocalDateTime.now()));
            log.info("订单 [{}] 超时未支付，已自动取消", order.getOrderNo());
        }
    }
}
