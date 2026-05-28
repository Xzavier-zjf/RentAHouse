package com.rental.user.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserLoginLog {
    private Long id;
    private Long userId;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime loginTime;
}
