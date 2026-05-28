package com.example.rentalgateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Value("${rental.services.user-url}")
    private String userServiceUrl;

    @Value("${rental.services.house-url}")
    private String houseServiceUrl;

    @Value("${rental.services.order-url}")
    private String orderServiceUrl;

    @Value("${rental.services.comment-url}")
    private String commentServiceUrl;

    @Value("${rental.services.message-url}")
    private String messageServiceUrl;

    @Bean
    public RouteLocator customRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // 用户服务
                .route("user-service", r -> r.path("/api/user/**")
                        .uri(userServiceUrl))
                .route("admin-user-service", r -> r.path("/api/admin/user/**", "/api/admin/account/**")
                        .uri(userServiceUrl))

                // 房源服务
                .route("house-service", r -> r.path("/api/house/**")
                        .uri(houseServiceUrl))
                .route("admin-house-service", r -> r.path("/api/admin/house/**")
                        .uri(houseServiceUrl))

                // 评论服务
                .route("comment-service", r -> r.path("/api/comment/**")
                        .uri(commentServiceUrl))
                .route("admin-comment-service", r -> r.path("/api/admin/comment/**")
                        .uri(commentServiceUrl))

                // 订单服务
                .route("order-service", r -> r.path("/api/order/**")
                        .uri(orderServiceUrl))
                .route("admin-order-service", r -> r.path("/api/admin/order/**")
                        .uri(orderServiceUrl))

                // 消息服务
                .route("message-service", r -> r.path("/api/message/**")
                        .uri(messageServiceUrl))
                .route("admin-message-service", r -> r.path("/api/admin/message/**")
                        .uri(messageServiceUrl))

                .build();
    }
}
