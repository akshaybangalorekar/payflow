package com.payflow.transactions.api.client;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * HTTP client for the accounts service. Balances live in the accounts
 * database; this service never touches another service's database.
 */
@Component
public class AccountsClient {

    private final RestClient restClient;

    public AccountsClient(@Value("${accounts.service.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public void debit(UUID accountId, BigDecimal amount) {
        restClient.post()
                .uri("/api/accounts/{id}/debit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity();
    }

    public void credit(UUID accountId, BigDecimal amount) {
        restClient.post()
                .uri("/api/accounts/{id}/credit", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("amount", amount))
                .retrieve()
                .toBodilessEntity();
    }
}
