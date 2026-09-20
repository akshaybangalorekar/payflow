package com.payflow.transactions.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.payflow.transactions.domain.LedgerEntry;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
}
