package com.example.rentalcomment.client;

import com.example.rentalcomment.vo.UserInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "rental-user", url = "${rental-user.url}")
public interface UserClient {
    @GetMapping("/api/user/info/{id}")
    UserInfoVO getUser(@PathVariable("id") Long id);
}
