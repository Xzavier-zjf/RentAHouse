package com.example.rentalorder;

import com.example.rentalorder.dto.CreateOrderRequest;
import com.example.rentalorder.service.OrderService;
import com.example.rentalcommon.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
public class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void testCreateOrderWhenHouseMissing() {
        CreateOrderRequest req = new CreateOrderRequest();
        req.setHouseId(Long.MAX_VALUE);
        req.setStartDate(LocalDate.of(2024, 7, 1));
        req.setEndDate(LocalDate.of(2024, 12, 1));
        req.setDeposit(new java.math.BigDecimal("3000"));

        assertThrows(BusinessException.class, () -> orderService.createOrder(1L, req));
    }
}
