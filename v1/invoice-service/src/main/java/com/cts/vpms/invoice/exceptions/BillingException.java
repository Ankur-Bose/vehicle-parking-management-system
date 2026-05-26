package com.cts.vpms.invoice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BillingException extends RuntimeException{
    private final HttpStatus status;
    private final String errorCode;

    public BillingException(String message, HttpStatus status, String errorCode) {
        super(message);
        this.status    = status;
        this.errorCode = errorCode;
    }
}