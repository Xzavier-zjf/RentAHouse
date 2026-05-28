package com.example.rentalorder;

import com.example.rentalorder.config.RabbitMQConfig;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class RabbitConsumerTest {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Test
    void testSendToDLQueue() {
        String fakeOrderNo = "ORD999999999";
        amqpTemplate.convertAndSend(
                RabbitMQConfig.ORDER_DL_EXCHANGE,
                RabbitMQConfig.ORDER_DL_ROUTING_KEY,
                fakeOrderNo
        );
    }
}
