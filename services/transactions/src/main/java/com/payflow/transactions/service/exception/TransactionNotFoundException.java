package com.payflow.transactions.service.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Object id) {
        super("transaction not found: " + id);
    }
}
