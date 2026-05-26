package com.cts.project.SlotManagement.exception;


import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SlotNotFoundException.class)
    public ResponseEntity<String> slotNotFoundHandler(SlotNotFoundException slotNotFoundException){
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(slotNotFoundException.getMessage());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<String> methodArgsMismatch(MethodArgumentTypeMismatchException methodArgumentTypeMismatchException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid value. ID must be a number.");
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodInvalid(MethodArgumentNotValidException methodArgumentNotValidException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(methodArgumentNotValidException.getFieldError().getDefaultMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<String> contraintViolation(ConstraintViolationException constraintViolationException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(constraintViolationException.getMessage());
    }


}
