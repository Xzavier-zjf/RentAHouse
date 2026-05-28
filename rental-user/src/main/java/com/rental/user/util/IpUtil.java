package com.rental.user.util;

import jakarta.servlet.http.HttpServletRequest;

public class IpUtil {

    private static final int MAX_IP_LENGTH = 45;

    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String[] headers = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                String first = ip.split(",")[0].trim();
                if (!first.isEmpty()) {
                    return truncate(first);
                }
            }
        }
        return truncate(request.getRemoteAddr());
    }

    private static String truncate(String ip) {
        if (ip == null) return null;
        return ip.length() > MAX_IP_LENGTH ? ip.substring(0, MAX_IP_LENGTH) : ip;
    }
}
