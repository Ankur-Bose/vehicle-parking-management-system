package com.cts.project.vpms.dto;

import com.cts.project.vpms.enums.Role;
import jakarta.annotation.Nullable;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserResponse {

    private final long id;
    private final String name;
    private final String email;
    private final String phoneNumber;
    private final Role role;
    private final @Nullable LocalDateTime createdAt;

    public UserResponse(long id, String name, String email,
                        String phoneNumber, Role role,
                        @Nullable LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = createdAt;
    }
}