package com.cts.project.vpms.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(Long id) {
        super("User not found with id "+id);
    }
}
