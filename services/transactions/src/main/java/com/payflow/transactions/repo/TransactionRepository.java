package com.payflow.transactions.repo;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.transactions.domain.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    boolean existsByIdempotencyKey(String idempotencyKey);

    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
}
