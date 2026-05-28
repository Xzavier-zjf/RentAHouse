package com.example.rentalcommon.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String role;

    public LoginUser(Long userId, String username) {
        this(userId, username, "user");
    }

    public LoginUser(Long userId, String username, String role) {
        this.userId = userId;
        this.username = username;
        this.role = role == null || role.isBlank() ? "user" : role;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(() -> "ROLE_" + role);
    }

    @Override
    public String getPassword() {
        return null; // JWT 无需密码
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
