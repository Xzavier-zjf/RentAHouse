package com.rental.user.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
    private String nickname;
    private String avatar;
    private String phone;
    private String email;
    private String oldPassword;
    private String newPassword;
}
