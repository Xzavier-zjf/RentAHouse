package com.rental.user.controller;

import com.rental.user.dto.UpdateUserRequest;
import com.rental.user.service.MongoFileStorageService;
import com.rental.user.vo.LoginResponse;
import com.rental.user.vo.UserInfoVO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.rental.user.dto.LoginRequest;
import com.rental.user.dto.RegisterRequest;
import com.rental.user.service.UserService;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final MongoFileStorageService mongoFileStorageService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        userService.register(request);
        return ResponseEntity.ok("注册成功");
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoVO> getUserInfo() {
        return ResponseEntity.ok(userService.getCurrentUserInfo());
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<UserInfoVO> getUserInfoById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserInfoById(id));
    }

    @PutMapping("/info")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateUserRequest request) {
        userService.updateUserInfo(request);
        return ResponseEntity.ok("更新成功");
    }

    @PostMapping("/upload-avatar")
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(mongoFileStorageService.storeAvatar(file));
    }

    @GetMapping("/file/{fileId}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileId) {
        GridFsResource resource = mongoFileStorageService.load(fileId);
        String contentType = resource.getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : resource.getContentType();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

}


