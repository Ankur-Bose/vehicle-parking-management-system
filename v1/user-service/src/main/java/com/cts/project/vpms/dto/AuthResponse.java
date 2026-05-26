package com.cts.project.vpms.dto;

import jakarta.annotation.Nullable;
import lombok.Getter;

//returned after Register/Login
@Getter
public class AuthResponse {
    private final String token;
    private final String role;
    private final long userId;
    private final @Nullable String message;

    public AuthResponse(String token, String role, long userId, @Nullable String message) {
        this.token = token;
        this.role = role;
        this.userId = userId;
        this.message = message;
    }
}