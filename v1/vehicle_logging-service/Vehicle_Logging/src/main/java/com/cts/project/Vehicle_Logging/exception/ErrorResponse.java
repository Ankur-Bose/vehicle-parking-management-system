package com.cts.project.Vehicle_Logging.exception;


import lombok.Getter;
import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;

    public ErrorResponse(int status, String error, String message, String path) {
        this.status    = status;
        this.error     = error;
        this.message   = message;
        this.path      = path;
        this.timestamp = LocalDateTime.now();
    }
}