package com.cts.project.vpms.service;

import com.cts.project.vpms.dto.AuthResponse;
import com.cts.project.vpms.dto.LoginRequest;
import com.cts.project.vpms.dto.RegisterRequest;
import com.cts.project.vpms.entity.User;

import java.util.List;

public interface RegisterService {
    AuthResponse register(RegisterRequest registerRequest);

}