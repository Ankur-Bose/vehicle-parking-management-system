package com.cts.project.vpms.service.serviceImpl;

import com.cts.project.vpms.dto.RegisterRequest;
import com.cts.project.vpms.entity.User;
import com.cts.project.vpms.exceptions.UserNotFoundException;
import com.cts.project.vpms.repository.UserRepository;
import com.cts.project.vpms.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
//    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
//        this.jwtUtil = jwtUtil;
    }

    @Override
    public List<User> getAllUser() {
        return userRepo.findAll();
    }

    @Override
    public User getUserById(long id) {
        return userRepo.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    @Override
    public User updateUser(long id, RegisterRequest registerRequest) {
        User user = getUserById(id);

        if (registerRequest.getName() != null && !registerRequest.getName().isBlank())
            user.setName(registerRequest.getName());

        if (registerRequest.getPhoneNumber() != null && !registerRequest.getPhoneNumber().isBlank())
            user.setPhoneNumber(registerRequest.getPhoneNumber());

        // Role is an enum — just null-check it
        if (registerRequest.getRole() != null)
            user.setRole(registerRequest.getRole());

        if (registerRequest.getPassword() != null && !registerRequest.getPassword().isBlank())
            user.setPassword_hash(passwordEncoder.encode(registerRequest.getPassword()));

        return userRepo.save(user);
    }

    @Override
    public void deleteUser(long id) {
        userRepo.deleteById(id);
    }
}