package io.github.unerdarks.services;

import io.github.unerdarks.models.RecurringTransaction;
import io.github.unerdarks.models.Transaction;
import io.github.unerdarks.repositories.RecurringTransactionRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RecurringTransactionService {

    private final RecurringTransactionRepository recurringTransactionRepository;
    private final TransactionService transactionService;

    public RecurringTransactionService(RecurringTransactionRepository recurringTransactionRepository, TransactionService transactionService) {
        this.recurringTransactionRepository = recurringTransactionRepository;
        this.transactionService = transactionService;
    }

    public List<Transaction> processDueOccurrences(Instant referenceDate) {
        List<Transaction> generated = new ArrayList<>();

        for (RecurringTransaction recurringTransaction : recurringTransactionRepository.findAll()) {
            while (recurringTransaction.isDue(referenceDate)) {
                Transaction occurrence = recurringTransaction.generateOccurrence();
                transactionService.create(occurrence);
                generated.add(occurrence);
            }

            recurringTransactionRepository.save(recurringTransaction);
        }

        return generated;
    }
}
