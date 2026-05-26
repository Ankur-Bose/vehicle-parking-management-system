package com.cts.project.reservationService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // validation errors → 400
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, errors);
    }

    // userException → 404
    @ExceptionHandler(UserException.class)
    public ResponseEntity<Map<String, Object>> handleUserException(UserException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // slotNotFound → 404
    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleSlotNotFound(SlotNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // slotAlreadyBooked → 409
    @ExceptionHandler(SlotAlreadyBookedException.class)
    public ResponseEntity<Map<String, Object>> handleSlotBooked(SlotAlreadyBookedException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // reservationNotFound → 404
    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReservationNotFound(ReservationNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    //handle role check / auth failures (401, 403, etc.)
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String message = ex.getReason() != null ? ex.getReason() : ex.getMessage();
        return buildResponse(HttpStatus.valueOf(status.value()), message);
    }

    // any other unexpected exception → 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong: " + ex.getMessage());
    }

    // helper to build consistent json error response
    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}