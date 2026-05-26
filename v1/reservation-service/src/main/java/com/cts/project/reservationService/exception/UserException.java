package com.cts.project.reservationService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserException extends RuntimeException{
    public UserException(Long id){
        super("User Not Found with id: "+id);
    }
}
