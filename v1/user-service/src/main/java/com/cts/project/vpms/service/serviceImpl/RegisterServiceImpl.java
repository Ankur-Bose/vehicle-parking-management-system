package com.cts.project.vpms.service.serviceImpl;

import com.cts.project.vpms.dto.AuthResponse;
import com.cts.project.vpms.dto.RegisterRequest;
import com.cts.project.vpms.entity.User;
import com.cts.project.vpms.exceptions.EmailAlreadyExistsException;
import com.cts.project.vpms.repository.UserRepository;
import com.cts.project.vpms.security.JwtUtil;
import com.cts.project.vpms.service.RegisterService;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private static final Logger log = LoggerFactory.getLogger(RegisterServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public AuthResponse register(RegisterRequest registerRequest) {
        log.info("Register attempt | email={}", registerRequest.getEmail());
        if(userRepository.existsByEmail(registerRequest.getEmail())) {
            log.warn("Email already exists | email={}", registerRequest.getEmail());
            throw new EmailAlreadyExistsException(registerRequest.getEmail());
        }

        User user = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(registerRequest.getRole())
                .password_hash(passwordEncoder.encode(registerRequest.getPassword()))
                .build();
        userRepository.save(user);
        log.info("User registered | email={}", user.getEmail());

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getRole().name(), user.getUserId(), "Registration Successful");
    }
}