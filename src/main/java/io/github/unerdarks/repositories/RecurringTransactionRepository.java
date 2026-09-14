package io.github.unerdarks.repositories;

import io.github.unerdarks.models.RecurringTransaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RecurringTransactionRepository {

    private final List<RecurringTransaction> recurringTransactions = new ArrayList<>();

    public RecurringTransaction save(RecurringTransaction recurringTransaction) {
        Optional<RecurringTransaction> existing = findById(recurringTransaction.getId());
        existing.ifPresent(recurringTransactions::remove);

        recurringTransactions.add(recurringTransaction);
        return recurringTransaction;
    }

    public Optional<RecurringTransaction> findById(UUID id) {
        return recurringTransactions.stream()
                .filter(recurringTransaction -> recurringTransaction.getId().equals(id))
                .findFirst();
    }

    public List<RecurringTransaction> findAll() {
        return List.copyOf(recurringTransactions);
    }
}
