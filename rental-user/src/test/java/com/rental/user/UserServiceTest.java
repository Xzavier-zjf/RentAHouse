package com.rental.user;

import com.rental.user.dto.LoginRequest;
import com.rental.user.dto.RegisterRequest;
import com.rental.user.service.UserService;
import com.rental.user.vo.LoginResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testRegisterAndLogin() {
        String suffix = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String username = "newuser_" + suffix;
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("123456");
        request.setEmail("test_" + suffix + "@xx.com");
        userService.register(request);

        LoginRequest login = new LoginRequest();
        login.setUsernameOrEmailOrPhone(username);
        login.setPassword("123456");

        LoginResponse response = userService.login(login);
        assertNotNull(response);
        assertNotNull(response.getToken());
    }
}
