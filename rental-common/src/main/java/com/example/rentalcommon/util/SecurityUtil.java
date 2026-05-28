package com.example.rentalcommon.util;

import com.example.rentalcommon.security.LoginUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;

public class SecurityUtil {

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getUserId();
        }
        throw new AuthenticationCredentialsNotFoundException("未登录");
    }

    public static String getCurrentUserRole() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser loginUser) {
            return loginUser.getRole();
        }
        throw new AuthenticationCredentialsNotFoundException("未登录");
    }

    public static boolean hasRole(String role) {
        return role != null && role.equalsIgnoreCase(getCurrentUserRole());
    }

    public static void requireRole(String role) {
        if (!hasRole(role)) {
            throw new AccessDeniedException("权限不足：需要" + role + "角色");
        }
    }

    public static void requireAnyRole(String... roles) {
        String currentRole = getCurrentUserRole();
        boolean allowed = Arrays.stream(roles)
                .anyMatch(role -> role != null && role.equalsIgnoreCase(currentRole));
        if (!allowed) {
            throw new AccessDeniedException("权限不足：需要角色 " + String.join("/", roles));
        }
    }
}
