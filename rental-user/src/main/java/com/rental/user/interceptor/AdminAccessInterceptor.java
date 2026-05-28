
package com.rental.user.interceptor;

import com.example.rentalcommon.annotation.RequireAdmin;
import com.example.rentalcommon.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AdminAccessInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (handler instanceof HandlerMethod handlerMethod) {
            RequireAdmin requireAdmin = handlerMethod.getMethodAnnotation(RequireAdmin.class);
            if (requireAdmin == null) {
                requireAdmin = handlerMethod.getBeanType().getAnnotation(RequireAdmin.class);
            }
            if (requireAdmin != null) {
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !(auth.getPrincipal() instanceof LoginUser user) || !"admin".equalsIgnoreCase(user.getRole())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("权限不足：需要管理员权限");
                    return false;
                }
            }
        }
        return true;
    }
}
