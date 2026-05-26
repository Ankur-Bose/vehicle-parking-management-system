package com.cts.project.vpms.exceptions;

public class EmailAlreadyExistsException extends RuntimeException{
    public EmailAlreadyExistsException(String email){
        super(email + " already exists.");
    }
}
