package com.payflow.accounts.api;

import com.payflow.accounts.service.exception.AccountNotFoundException;
import com.payflow.accounts.service.exception.DuplicateAccountException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    record ApiError(String code, String message, Instant timestamp) {}

    @ExceptionHandler(AccountNotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(AccountNotFoundException ex) {
        return respond(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(DuplicateAccountException.class)
    ResponseEntity<ApiError> handleDuplicate(DuplicateAccountException ex) {
        return respond(HttpStatus.CONFLICT, "DUPLICATE_ACCOUNT", ex.getMessage());
    }

    @ExceptionHandler (DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex) {
        return respond(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", ex.getMessage());
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
