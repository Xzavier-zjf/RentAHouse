package com.example.rentalorder.mq;

import com.example.rentalorder.service.OrderService;
import com.example.rentalorder.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutConsumer {

    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConfig.ORDER_DL_QUEUE)
    public void handleOrderTimeout(String orderNo) {
        log.info("收到超时订单处理消息：{}", orderNo);
        orderService.cancelOrderByOrderNo(orderNo);
    }
}
