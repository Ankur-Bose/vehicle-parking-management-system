package com.cts.project.vpms.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            UserNotFoundException ex, HttpServletRequest req) {
        log.warn("User not found | path={}", req.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled error | path={} | msg={}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Something went wrong.", req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), error, message, path));
    }

    @ExceptionHandler(JwtTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleJwtExpired(
            JwtTokenExpiredException ex, HttpServletRequest req) {
        log.warn("Expired JWT | path={}", req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "Token Expired", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(JwtTokenUnsupportedException.class)
    public ResponseEntity<ErrorResponse> handleJwtUnsupported(
            JwtTokenUnsupportedException ex, HttpServletRequest req) {
        log.warn("Unsupported JWT | path={}", req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "Unsupported Token", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(JwtSignatureInvalidException.class)
    public ResponseEntity<ErrorResponse> handleJwtSignature(
            JwtSignatureInvalidException ex, HttpServletRequest req) {
        log.warn("Invalid JWT signature | path={}", req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "Invalid Signature", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(JwtTokenEmptyException.class)
    public ResponseEntity<ErrorResponse> handleJwtEmpty(
            JwtTokenEmptyException ex, HttpServletRequest req) {
        log.warn("Empty JWT | path={}", req.getRequestURI());
        return build(HttpStatus.UNAUTHORIZED, "Missing Token", ex.getMessage(), req.getRequestURI());
    }
}