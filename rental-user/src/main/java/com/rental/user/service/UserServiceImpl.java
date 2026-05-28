package com.rental.user.service;

import com.example.rentalcommon.exception.BusinessException;
import com.rental.user.dto.RegisterRequest;
import com.rental.user.dto.UpdateUserRequest;
import com.rental.user.entity.UserLoginLog;
import com.rental.user.mapper.UserLoginLogMapper;
import com.rental.user.util.IpUtil;
import com.example.rentalcommon.util.JwtUtil;
import com.rental.user.util.RequestContextHolderUtil;
import com.example.rentalcommon.util.SecurityUtil;
import com.rental.user.vo.LoginResponse;
import com.rental.user.vo.UserInfoVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;
import com.rental.user.dto.LoginRequest;
import com.rental.user.entity.User;
import com.rental.user.mapper.UserMapper;




import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    private UserLoginLogMapper loginLogMapper;

    @Override
    public void register(RegisterRequest request) {
        if (userMapper.selectByUsername(request.getUsername()) != null) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setRole(normalizeRegisterRole(request.getRole()));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userMapper.selectByUsername(request.getUsernameOrEmailOrPhone());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("账号已被禁用");
        }

        String role = normalizeStoredRole(user.getRole());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), role);

        // 插入登录日志
        HttpServletRequest httpRequest = RequestContextHolderUtil.getHttpServletRequest();
        String ip = IpUtil.getClientIp(httpRequest);
        String ua = httpRequest.getHeader("User-Agent");

        UserLoginLog log = new UserLoginLog();
        log.setUserId(user.getId());
        log.setIpAddress(ip);
        log.setUserAgent(ua);
        log.setLoginTime(LocalDateTime.now());
        loginLogMapper.insert(log);

        return new LoginResponse(token, toUserInfoVO(user));
    }

    @Override
    public UserInfoVO getCurrentUserInfo() {
        Long userId = SecurityUtil.getCurrentUserId();
        return getUserInfoById(userId);
    }

    @Override
    public UserInfoVO getUserInfoById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");
        return toUserInfoVO(user);
    }

    @Override
    public void updateUserInfo(UpdateUserRequest request) {
        Long userId = SecurityUtil.getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException("用户不存在");

        if (StringUtils.hasText(request.getOldPassword()) &&
                StringUtils.hasText(request.getNewPassword())) {
            if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
                throw new BusinessException("旧密码错误");
            }
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        }

        if (StringUtils.hasText(request.getPhone())) user.setPhone(request.getPhone());
        if (StringUtils.hasText(request.getEmail())) user.setEmail(request.getEmail());
        if (StringUtils.hasText(request.getNickname())) user.setRealName(request.getNickname());
        if (StringUtils.hasText(request.getAvatar())) user.setAvatar(request.getAvatar());

        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    private UserInfoVO toUserInfoVO(User user) {
        UserInfoVO vo = new UserInfoVO();
        BeanUtils.copyProperties(user, vo);
        vo.setNickname(StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername());
        vo.setRole(normalizeStoredRole(user.getRole()));
        return vo;
    }

    private String normalizeRegisterRole(String role) {
        if ("owner".equalsIgnoreCase(role) || "landlord".equalsIgnoreCase(role)) {
            return "owner";
        }
        return "user";
    }

    private String normalizeStoredRole(String role) {
        return StringUtils.hasText(role) ? role : "user";
    }
}
