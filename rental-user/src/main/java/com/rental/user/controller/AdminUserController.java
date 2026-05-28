package com.rental.user.controller;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.rentalcommon.annotation.OperationLog;
import com.example.rentalcommon.annotation.RequireAdmin;
import com.example.rentalcommon.util.PaginationUtil;
import com.rental.user.entity.User;
import com.rental.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequireAdmin
@RestController
@RequestMapping("/api/admin/user")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserMapper userMapper;

    @GetMapping("/list")
    public ResponseEntity<IPage<User>> list(@RequestParam int page, @RequestParam int size) {
        Page<User> pg = new Page<>(PaginationUtil.normalizePage(page), PaginationUtil.normalizeSize(size));
        IPage<User> users = userMapper.selectPage(pg, null);
        users.getRecords().forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    @OperationLog("禁用用户")
    @PostMapping("/disable/{userId}")
    public ResponseEntity<?> disableUser(@PathVariable Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getStatus, 0));
        return ResponseEntity.ok("禁用成功");
    }


    @PostMapping("/enable/{userId}")
    public ResponseEntity<?> enableUser(@PathVariable Long userId) {
        userMapper.update(null, new LambdaUpdateWrapper<User>()
                .eq(User::getId, userId)
                .set(User::getStatus, 1));
        return ResponseEntity.ok("启用成功");
    }
}
