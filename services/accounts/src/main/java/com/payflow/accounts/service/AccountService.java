package com.payflow.accounts.service;

import com.payflow.accounts.api.dto.CreateAccountRequest;
import com.payflow.accounts.domain.Account;
import com.payflow.accounts.repo.AccountRepository;
import com.payflow.accounts.service.exception.AccountNotFoundException;
import com.payflow.accounts.service.exception.DuplicateAccountException;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public Account create(CreateAccountRequest request) {
        if (accountRepository.existsByEmail(request.email())) {
            throw new DuplicateAccountException(request.email());
        }
        Account account = new Account(
                request.customerName(),
                request.email(),
                request.openingBalance()
        );
        return accountRepository.save(account);
    }   

    public Account getById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(id));
    }
}
