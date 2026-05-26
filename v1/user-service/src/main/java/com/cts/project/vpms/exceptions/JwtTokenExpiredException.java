package com.cts.project.vpms.exceptions;

public class JwtTokenExpiredException extends RuntimeException {
    public JwtTokenExpiredException() {
        super("JWT token has expired. Please log in again.");
    }
}