package io.github.unerdarks.services;

import io.github.unerdarks.enums.TransactionStatus;
import io.github.unerdarks.models.CreditCard;
import io.github.unerdarks.models.Transaction;
import io.github.unerdarks.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
    }

    public Transaction create(Transaction transaction) {
        if (transactionRepository.existsById(transaction.getId())) {
            throw new IllegalArgumentException("Transaction already exists");
        }


        return transactionRepository.save(transaction);
    }

    public Transaction update(UUID id, Transaction updatedTransaction) {
        Transaction existingTransaction = findById(id);

        if (!existingTransaction.canBeUpdated()) {
            throw new IllegalArgumentException("Transaction cannot be updated");
        }

        return transactionRepository.save(updatedTransaction);
    }

    public void delete(UUID id) {
        Transaction transaction = findById(id);

        if (transaction.CanBeDeleted()) {
            transactionRepository.delete(transaction);
        } else {
            throw new IllegalArgumentException("Transaction cannot be deleted");
        }

    }

    private Transaction findById(UUID id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));
    }

    public List<Transaction> activateDueInstallments(Instant referenceDate) {
        List<Transaction> dueInstallments = transactionRepository.findAll().stream()
                .filter(Transaction::isScheduled)
                .filter(transaction -> !transaction.getDate().isAfter(referenceDate))
                .toList();

        dueInstallments.forEach(Transaction::activate);
        dueInstallments.forEach(transactionRepository::save);

        return dueInstallments;
    }

}
