package com.cts.project.vpms.service;

import com.cts.project.vpms.dto.RegisterRequest;
import com.cts.project.vpms.entity.User;

import java.util.List;

public interface UserService {
    List<User> getAllUser();
    User getUserById(long id);
    User getUserByEmail(String email);
    User updateUser(long id, RegisterRequest registerRequest);
    void deleteUser(long id);
}