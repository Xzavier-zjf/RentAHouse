package com.rental.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("user")
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String password;
    private String phone;
    private String email;
    private String avatar;
    private String realName;
    private String idCard;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String role;
}
