package com.rental.user.security;

import com.example.rentalcommon.security.LoginUser;
import com.example.rentalcommon.util.JwtUtil;
import com.rental.user.entity.User;
import com.rental.user.mapper.UserMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String token = request.getHeader("Authorization");

        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
            token = token.substring(7);

            try {
                Long userId = jwtUtil.getUserId(token);
                String username = jwtUtil.getUsername(token);
                String tokenRole = jwtUtil.getRole(token);

                User user = userMapper.selectById(userId);

                if (user != null && (user.getStatus() == null || user.getStatus() == 1)) {
                    String role = user.getRole() == null || user.getRole().isBlank() ? tokenRole : user.getRole();
                    LoginUser loginUser = new LoginUser(userId, username, role);

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }

            } catch (Exception e) {
                logger.warn("无效的Token：" + token, e);
            }
        }

        filterChain.doFilter(request, response);
    }
}
