package com.payflow.accounts.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.payflow.accounts.domain.Account;

public record AccountResponse(
        UUID id,
        String customerName,
        String email,
        BigDecimal balance
        ) {

    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerName(),
                account.getEmail(),
                account.getBalance()
        );
    }
}
