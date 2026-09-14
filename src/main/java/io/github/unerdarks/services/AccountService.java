package io.github.unerdarks.services;

import io.github.unerdarks.models.Account;
import io.github.unerdarks.repositories.AccountRepository;
import io.github.unerdarks.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository, TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    public void transfer(UUID from, UUID to, BigDecimal amount) {
        Account accountFrom = getById(from);
        Account accountTo = getById(to);

        if (accountFrom == null || accountTo == null) {
            throw new IllegalArgumentException("One or both accounts not found");
        }

        if (accountFrom.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        accountFrom.withdraw(amount);
        accountTo.deposit(amount);

    }

    public Account getById(UUID id) {
        return accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
    }

    public void delete(UUID id) {
        Account account = getById(id);

        if (transactionRepository.existsByAccount(id)) {
            throw new IllegalArgumentException("Cannot delete an account with linked transactions; deactivate it instead");
        }

        accountRepository.deleteById(account.getId());
    }

}