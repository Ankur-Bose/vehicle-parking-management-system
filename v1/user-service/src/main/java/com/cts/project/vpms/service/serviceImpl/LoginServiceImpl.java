package com.cts.project.vpms.service.serviceImpl;

import com.cts.project.vpms.dto.AuthResponse;
import com.cts.project.vpms.dto.LoginRequest;
import com.cts.project.vpms.entity.User;
import com.cts.project.vpms.repository.UserRepository;
import com.cts.project.vpms.security.JwtUtil;
import com.cts.project.vpms.service.LoginService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class LoginServiceImpl implements LoginService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse login(@RequestBody LoginRequest loginRequest) {

        System.out.println("LOGIN EMAIL = [" + loginRequest.getEmail() + "]");

        User user = userRepo.findByEmail(loginRequest.getEmail()).orElseThrow(
                () -> new RuntimeException("User does not exist")
        );

        if(!(passwordEncoder.matches(loginRequest.getPassword(), user.getPassword_hash()))) {
            throw new RuntimeException("Credentials doesn't match");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getUserId(), "Login Successful");
    }
}
