package com.rental.user.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

public class RequestContextHolderUtil {
    public static HttpServletRequest getHttpServletRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new IllegalStateException("当前线程无 HTTP 请求上下文");
        }
        return ((ServletRequestAttributes) attrs).getRequest();
    }
}
