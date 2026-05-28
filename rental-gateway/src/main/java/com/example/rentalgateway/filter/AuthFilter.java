package com.example.rentalgateway.filter;

import com.example.rentalcommon.util.JwtUtil;
import jakarta.annotation.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpMethod;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtil jwtUtil;

    private final List<String> whitelist = List.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/file/",
            "/api/house/search",
            "/api/house/hot",
            "/api/house/file/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, org.springframework.cloud.gateway.filter.GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (exchange.getRequest().getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        if (whitelist.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        if (exchange.getRequest().getMethod() == HttpMethod.GET && path.matches("/api/house/\\d+")) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            jwtUtil.getUserId(token);
            String role = jwtUtil.getRole(token);
            if (path.startsWith("/api/admin/") && !"admin".equalsIgnoreCase(role)) {
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }
        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // 最高优先级
    }
}
