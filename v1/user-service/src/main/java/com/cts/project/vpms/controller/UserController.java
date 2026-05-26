package com.cts.project.vpms.controller;

import com.cts.project.vpms.dto.AuthResponse;
import com.cts.project.vpms.dto.LoginRequest;
import com.cts.project.vpms.dto.RegisterRequest;
import com.cts.project.vpms.dto.UserResponse;
import com.cts.project.vpms.entity.User;
import com.cts.project.vpms.service.LoginService;
import com.cts.project.vpms.service.RegisterService;
import com.cts.project.vpms.service.UserService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final RegisterService registerService;
    private final LoginService loginService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        log.info("POST /users/register | email={}", registerRequest.getEmail());
        return ResponseEntity.ok(registerService.register(registerRequest));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("POST /users/login | email={}", loginRequest.getEmail());
        return ResponseEntity.ok(loginService.login(loginRequest));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMe(@AuthenticationPrincipal String email) {
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt()
        ));
    }

    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(
                userService.getAllUser()
                        .stream()
                        .map(user -> new UserResponse(
                                user.getUserId(),
                                user.getName(),
                                user.getEmail(),
                                user.getPhoneNumber(),
                                user.getRole(),
                                user.getCreatedAt()
                        ))
                        .toList()
        );
    }
    // Internal endpoint — only for service-to-service calls
    @GetMapping("/internal/{id}")
    public ResponseEntity<User> getUserByIdInternal(@PathVariable long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'CUSTOMER')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(new UserResponse(
                user.getUserId(),
                user.getName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt()
        ));
    }

    @PutMapping("/updateUser/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<String> updateUser(@PathVariable long id,
                                                   @RequestBody RegisterRequest registerRequest) {
        log.info("PUT /users/{} | Updating user", id);
        User updated = userService.updateUser(id, registerRequest);  // ← called once only

        UserResponse response = new UserResponse(
                updated.getUserId(),
                updated.getName(),
                updated.getEmail(),
                updated.getPhoneNumber(),
                updated.getRole(),
                updated.getCreatedAt()
        );

        return ResponseEntity.ok("User Details updated successfully");  // ← return the DTO, not the raw User
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully.");
    }
}