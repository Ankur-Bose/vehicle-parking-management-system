package com.cts.project.Vehicle_Logging.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {



    @ExceptionHandler(VehicleLogNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            VehicleLogNotFoundException ex, HttpServletRequest req) {
        log.warn("Not found | path={}", req.getRequestURI());
        return build(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(SlotNotAvailableException.class)
    public ResponseEntity<ErrorResponse> handleSlotUnavailable(
            SlotNotAvailableException ex, HttpServletRequest req) {
        log.warn("Slot unavailable | path={}", req.getRequestURI());
        return build(HttpStatus.CONFLICT, "Slot Unavailable", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ActiveSessionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleActiveSession(
            ActiveSessionAlreadyExistsException ex, HttpServletRequest req) {
        log.warn("Duplicate session | path={}", req.getRequestURI());
        return build(HttpStatus.CONFLICT, "Session Conflict", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NoReservationFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoReservation(
            NoReservationFoundException ex, HttpServletRequest req) {
        log.warn("No reservation | path={}", req.getRequestURI());
        return build(HttpStatus.FORBIDDEN, "No Reservation", ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailable(
            ServiceUnavailableException ex, HttpServletRequest req) {
        log.error("Downstream service unavailable | path={}", req.getRequestURI());
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        log.warn("Validation failed | path={} errors={}", req.getRequestURI(), msg);
        return build(HttpStatus.BAD_REQUEST, "Validation Error", msg, req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex, HttpServletRequest req) {
        log.error("Unhandled error | path={}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Something went wrong.", req.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String error, String message, String path) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), error, message, path));
    }
}