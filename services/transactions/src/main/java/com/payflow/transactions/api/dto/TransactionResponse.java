package com.payflow.transactions.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.payflow.transactions.domain.Transaction;
import com.payflow.transactions.domain.TransactionStatus;

public record TransactionResponse(
        UUID id,
        UUID fromAccount,
        UUID toAccount,
        BigDecimal amount,
        TransactionStatus status
) {
    public static TransactionResponse from(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getFromAccount(),
                t.getToAccount(),
                t.getAmount(),
                t.getStatus()
        );
    }
}
