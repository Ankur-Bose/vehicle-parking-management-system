package com.cts.project.vpms.exceptions;

public class JwtSignatureInvalidException extends RuntimeException{
    public JwtSignatureInvalidException(){
        super("JWT token signature is invalid. Token may have been tampered with.");
    }
}
