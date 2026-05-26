package com.cts.project.vpms.dto;

import com.cts.project.vpms.enums.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String phoneNumber;
    private String password;
    private Role role;
}

