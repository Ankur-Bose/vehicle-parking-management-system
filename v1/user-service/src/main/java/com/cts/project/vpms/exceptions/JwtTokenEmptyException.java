package com.cts.project.vpms.exceptions;

public class JwtTokenEmptyException extends RuntimeException{
    public JwtTokenEmptyException() {
        super("Jwt token has not been inserted");
    }
}