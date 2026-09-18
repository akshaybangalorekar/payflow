package com.payflow.accounts.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateAccountRequest(
        @NotBlank(message = "customerName is required") String customerName,
        @NotBlank @Email(message = "email must be a valid address") String email,
        @NotNull @PositiveOrZero(message = "openingBalance cannot be negative") Double openingBalance
) {
}
