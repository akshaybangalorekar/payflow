package com.payflow.accounts.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String customerName;

    private String email;

    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    private AccountStatus status;

    /** Internal use only: fraud/ops notes. Never shown to customers. */
    private String internalNotes;

    private Instant createdAt;

    protected Account() {
    }

    public Account(String customerName, String email, BigDecimal openingBalance) {
        this.customerName = customerName;
        this.email = email;
        this.balance = openingBalance;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void debit(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.balance = this.balance.add(amount);
    }

    public UUID getId() { return id; }
    public String getCustomerName() { return customerName; }
    public String getEmail() { return email; }
    public BigDecimal getBalance() { return balance; }
    public AccountStatus getStatus() { return status; }
    public String getInternalNotes() { return internalNotes; }
    public Instant getCreatedAt() { return createdAt; }

    public void setInternalNotes(String internalNotes) { this.internalNotes = internalNotes; }
}
