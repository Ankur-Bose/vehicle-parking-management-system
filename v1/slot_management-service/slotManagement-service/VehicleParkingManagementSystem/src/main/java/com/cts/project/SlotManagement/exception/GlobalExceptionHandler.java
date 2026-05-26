package com.cts.project.SlotManagement.exception;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<String> invalidInputHandler(InvalidInputException invalidInputException){
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(invalidInputException.getMessage());
    }


}
