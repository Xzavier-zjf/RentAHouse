package com.example.rentalorder.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.Queue; // 正确的 Queue 类型（来自 Spring AMQP）


@Configuration
public class RabbitMQConfig {

    // 1. 正常延迟消息队列（带 TTL）
    public static final String ORDER_DELAY_QUEUE = "order.delay.queue";
    public static final String ORDER_DELAY_EXCHANGE = "order.delay.exchange";
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay.routing";

    // 2. 死信队列
    public static final String ORDER_DL_QUEUE = "order.dl.queue";
    public static final String ORDER_DL_EXCHANGE = "order.dl.exchange";
    public static final String ORDER_DL_ROUTING_KEY = "order.dl.routing";

    // 正常交换机
    @Bean
    public DirectExchange orderDelayExchange() {
        return new DirectExchange(ORDER_DELAY_EXCHANGE);
    }

    // 死信交换机
    @Bean
    public DirectExchange orderDLExchange() {
        return new DirectExchange(ORDER_DL_EXCHANGE);
    }

    // 正常延迟队列
    @Bean
    public Queue orderDelayQueue() {
        return QueueBuilder.durable(ORDER_DELAY_QUEUE)
                .withArgument("x-dead-letter-exchange", ORDER_DL_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ORDER_DL_ROUTING_KEY)
                .withArgument("x-message-ttl", 15 * 60 * 1000) // 15分钟 TTL
                .build();
    }

    // 死信队列
    @Bean
    public Queue orderDLQueue() {
        return QueueBuilder.durable(ORDER_DL_QUEUE).build();
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderDelayExchange()).with(ORDER_DELAY_ROUTING_KEY);
    }

    @Bean
    public Binding orderDLBinding() {
        return BindingBuilder.bind(orderDLQueue()).to(orderDLExchange()).with(ORDER_DL_ROUTING_KEY);
    }
}
