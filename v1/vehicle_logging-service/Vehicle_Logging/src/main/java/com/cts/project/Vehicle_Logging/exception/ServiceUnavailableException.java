package com.cts.project.Vehicle_Logging.exception;


public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
