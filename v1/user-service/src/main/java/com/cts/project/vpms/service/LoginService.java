package com.cts.project.vpms.service;

import com.cts.project.vpms.dto.AuthResponse;
import com.cts.project.vpms.dto.LoginRequest;

public interface LoginService {
    AuthResponse login(LoginRequest loginRequest);
}