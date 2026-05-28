package com.example.rentalcommon.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Date;

@Component
public class JwtUtil {

    private static final String DEFAULT_SECRET = "local-dev-rental-platform-secret-change-me";
    private static final long DEFAULT_EXPIRATION_SECONDS = 7200;

    private String secretKey = configuredSecret();
    private long expirationTime = DEFAULT_EXPIRATION_SECONDS * 1000;

    @Value("${rental.jwt.secret:${jwt.secret:}}")
    public void setSecretKey(String secretKey) {
        if (StringUtils.hasText(secretKey)) {
            this.secretKey = secretKey;
        }
    }

    @Value("${rental.jwt.expire-seconds:${jwt.expire:7200}}")
    public void setExpirationSeconds(long expirationSeconds) {
        this.expirationTime = Math.max(60, expirationSeconds) * 1000;
    }


    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "user");
    }

    public String generateToken(Long userId, String username, String role) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role == null || role.isBlank() ? "user" : role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseToken(token).getSubject());
    }

    public String getUsername(String token) {
        return parseToken(token).get("username", String.class);
    }

    public String getRole(String token) {
        String role = parseToken(token).get("role", String.class);
        return role == null || role.isBlank() ? "user" : role;
    }

    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }

    private Claims parseToken(String token) {
        return Jwts.parser()
                .setSigningKey(secretKey)
                .parseClaimsJws(token)
                .getBody();
    }

    private static String configuredSecret() {
        String propertySecret = System.getProperty("rental.jwt.secret");
        if (StringUtils.hasText(propertySecret)) {
            return propertySecret;
        }
        String envSecret = System.getenv("RENTAL_JWT_SECRET");
        return StringUtils.hasText(envSecret) ? envSecret : DEFAULT_SECRET;
    }
}
