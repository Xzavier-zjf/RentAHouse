package com.rental.user.service;

import com.rental.user.dto.LoginRequest;
import com.rental.user.dto.RegisterRequest;
import com.rental.user.dto.UpdateUserRequest;
import com.rental.user.vo.LoginResponse;
import com.rental.user.vo.UserInfoVO;

public interface UserService {
    void register(RegisterRequest dto);
    LoginResponse login(LoginRequest dto);
    UserInfoVO getCurrentUserInfo();
    UserInfoVO getUserInfoById(Long userId);
    void updateUserInfo(UpdateUserRequest request);
}
