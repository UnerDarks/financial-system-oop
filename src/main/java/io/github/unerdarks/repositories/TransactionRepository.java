package io.github.unerdarks.repositories;

import io.github.unerdarks.models.Account;
import io.github.unerdarks.models.Transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TransactionRepository {

    private final List<Transaction> transactions = new ArrayList<>();

    public Transaction save (Transaction transaction){
        Optional<Transaction> existingTransaction = findById(transaction.getId());
        existingTransaction.ifPresent(transactions::remove);

        transactions.add(transaction);
        return transaction;
    }

    public List<Transaction> findOpenByAccount(UUID accountId) {
        return transactions.stream()
                .filter(transaction -> transaction.getAccount().getId().equals(accountId))
                .toList();
    }

    public Optional<Transaction> findById(UUID id){
        return transactions.stream()
                .filter(transaction -> transaction.getId().equals(id))
                .findFirst();
    }

    public List<Transaction> findAll() {
        return List.copyOf(transactions);
    }

    public void delete(Transaction transaction) {
        transactions.remove(transaction);
    }

    public void deleteById(UUID id){
        Optional<Transaction> transaction = findById(id);
        transaction.ifPresent(this::delete);
    }

    public boolean existsById(UUID id) {
        return findById(id).isPresent();
    }

    public boolean existsByAccount(UUID accountId) {
        return transactions.stream()
                .anyMatch(transaction -> transaction.getAccount().getId().equals(accountId));
    }
}
