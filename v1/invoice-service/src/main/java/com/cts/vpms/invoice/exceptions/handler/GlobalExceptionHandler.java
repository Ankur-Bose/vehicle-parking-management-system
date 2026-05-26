package com.cts.vpms.invoice.exceptions.handler;

import com.cts.vpms.invoice.exceptions.BillingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BillingException.class)
    public ProblemDetail handleBillingException(BillingException ex) {
        log.warn("[{}] {}", ex.getErrorCode(), ex.getMessage());

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(ex.getStatus(), ex.getMessage());
        pd.setTitle(ex.getStatus().getReasonPhrase());
        pd.setProperty("errorCode", ex.getErrorCode());
        pd.setProperty("timestamp", LocalDateTime.now().toString());
        return pd;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        Map<String, String> errors = new HashMap<String, String>();
        for (FieldError fe : fieldErrors) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }

        log.info("[VALIDATION_ERROR] fields={}", errors);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request body has validation errors. See fieldErrors for details."
        );
        pd.setTitle("Validation Failed");
        pd.setProperty("errorCode",   "VALIDATION_ERROR");
        pd.setProperty("fieldErrors", errors);
        pd.setProperty("timestamp",   LocalDateTime.now().toString());
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String detail = "Parameter '" + ex.getName() + "' has invalid value: '" + ex.getValue() + "'";
        log.info("[BAD_REQUEST] {}", detail);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Bad Request");
        pd.setProperty("errorCode", "INVALID_PARAMETER");
        pd.setProperty("timestamp", LocalDateTime.now().toString());
        return pd;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        log.error("[UNEXPECTED_ERROR] {}", ex.getMessage(), ex);

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        pd.setTitle("Internal Server Error");
        pd.setProperty("errorCode", "INTERNAL_ERROR");
        pd.setProperty("timestamp", LocalDateTime.now().toString());
        return pd;
    }
}