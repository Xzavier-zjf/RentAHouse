package com.example.rentalorder.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "rental-message", url = "${rental-message.url}")
public interface MessageClient {
    @PostMapping("/api/message/system")
    void sendSystemMessage(@RequestParam Long toUserId, @RequestParam String content);
}

