package com.payflow.transactions.service;

import java.util.Optional;
import java.util.UUID;

import com.payflow.transactions.api.client.AccountsClient;
import com.payflow.transactions.api.dto.CreateTransactionRequest;
import com.payflow.transactions.domain.Direction;
import com.payflow.transactions.domain.LedgerEntry;
import com.payflow.transactions.domain.Transaction;
import com.payflow.transactions.repo.LedgerEntryRepository;
import com.payflow.transactions.repo.TransactionRepository;
import com.payflow.transactions.service.exception.TransactionNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final AccountsClient accountsClient;

    public TransactionService(TransactionRepository transactionRepository,
                              LedgerEntryRepository ledgerEntryRepository,
                              AccountsClient accountsClient) {
        this.transactionRepository = transactionRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.accountsClient = accountsClient;
    }

    public Transaction create(String idempotencyKey, CreateTransactionRequest request) {
        if (idempotencyKey != null) {
            Optional<Transaction> existing = transactionRepository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                // A retry with the same key: don't charge anyone twice.
                return existing.get();
            }
        }

        accountsClient.debit(request.fromAccountId(), request.amount());
        accountsClient.credit(request.toAccountId(), request.amount());

        Transaction transaction = new Transaction(
                idempotencyKey,
                request.fromAccountId(),
                request.toAccountId(),
                request.amount()
        );
        Transaction saved = transactionRepository.save(transaction);

        ledgerEntryRepository.save(new LedgerEntry(
                saved.getId(), request.fromAccountId(), Direction.DEBIT, request.amount()));
        ledgerEntryRepository.save(new LedgerEntry(
                saved.getId(), request.toAccountId(), Direction.CREDIT, request.amount()));

        return saved;
    }

    public Transaction getById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }
}
