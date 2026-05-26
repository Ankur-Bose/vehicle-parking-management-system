package com.cts.project.vpms.exceptions;

public class JwtTokenUnsupportedException extends RuntimeException{
    public JwtTokenUnsupportedException(){
        super("JWT token format is not supported.");
    }
}
