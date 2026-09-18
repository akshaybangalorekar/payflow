package com.payflow.accounts.service.exception;

public class DuplicateAccountException extends RuntimeException {
    public DuplicateAccountException(String email) {
        super("An account already exists for email: " + email);
    }
}
