package com.payflow.transactions.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import com.payflow.transactions.service.exception.TransactionNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientResponseException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(String code, String message, Instant timestamp) {}

    @ExceptionHandler(TransactionNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(TransactionNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, "TRANSACTION_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(RestClientResponseException.class)
    ResponseEntity<ApiError> handleUpstream(RestClientResponseException ex) {
        return respond(HttpStatus.BAD_GATEWAY, "UPSTREAM_ERROR",
                "accounts service returned " + ex.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest()
                .body(new ApiError("VALIDATION_FAILED", fieldErrors.toString(), Instant.now()));
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String code, String message) {
        return ResponseEntity.status(status)
                .body(new ApiError(code, message, Instant.now()));
    }
}
