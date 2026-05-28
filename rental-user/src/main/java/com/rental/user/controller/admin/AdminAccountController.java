package com.rental.user.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.rentalcommon.annotation.RequireAdmin;
import com.rental.user.dto.RegisterRequest;
import com.rental.user.entity.User;
import com.rental.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RequireAdmin
@RestController
@RequestMapping("/api/admin/account")
@RequiredArgsConstructor
public class AdminAccountController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/create")
    public ResponseEntity<?> createAdmin(@RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRole("admin");
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        return ResponseEntity.ok("管理员创建成功");
    }

    @GetMapping("/list")
    public ResponseEntity<List<User>> listAdmins() {
        List<User> admins = userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "admin"));
        admins.forEach(admin -> admin.setPassword(null));
        return ResponseEntity.ok(admins);
    }
}
