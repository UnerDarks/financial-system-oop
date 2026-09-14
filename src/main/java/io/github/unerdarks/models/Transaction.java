package io.github.unerdarks.models;

import io.github.unerdarks.enums.CategoryType;
import io.github.unerdarks.enums.TransactionStatus;
import io.github.unerdarks.enums.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Transaction {
    private UUID id;
    private Account account;
    private Category category;
    private BigDecimal amount;
    private TransactionType type;
    private Instant date;
    private String description;
    private TransactionStatus status;
    private boolean reconciled;

    private Transaction() {
        this.id = UUID.randomUUID();
    }

    public static Transaction create(Account account, Category category, BigDecimal amount, TransactionType type, Instant date, String description) {
        return build(account, category, amount, type, date, description, TransactionStatus.PENDING);
    }

    public static Transaction createScheduled(Account account, Category category, BigDecimal amount, TransactionType type, Instant date, String description) {
        return build(account, category, amount, type, date, description, TransactionStatus.SCHEDULED);
    }

    private static Transaction build(Account account, Category category, BigDecimal amount, TransactionType type, Instant date, String description, TransactionStatus status) {
        Transaction transaction = new Transaction();

        transaction.account = account;
        transaction.category = category;
        transaction.amount = amount;
        transaction.type = type;
        transaction.date = date;
        transaction.description = description;
        transaction.status = status;
        transaction.reconciled = false;

        transaction.validate();

        return transaction;
    }

    public void activate() {
        if (this.status != TransactionStatus.SCHEDULED) {
            throw new IllegalStateException("Only scheduled transactions can be activated");
        }

        this.status = TransactionStatus.PENDING;
    }

    private void validate() {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (amount == null) {
            throw new IllegalArgumentException("Amount cannot be null");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (type == TransactionType.INCOME && category.getType() != CategoryType.INCOME) {
            throw new IllegalArgumentException("An expense category cannot be used in an income transaction");
        }
        if (type == TransactionType.EXPENSE && category.getType() != CategoryType.EXPENSE) {
            throw new IllegalArgumentException("An income category cannot be used in an expense transaction");
        }
        if (date.isAfter(Instant.now()) && status != TransactionStatus.PENDING && status != TransactionStatus.SCHEDULED) {
            throw new IllegalArgumentException("A future-dated transaction can only be pending or scheduled");
        }

    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Category getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionType getType() {
        return type;
    }

    public Instant getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public boolean isReconciled() {
        return reconciled;
    }

    public boolean isCanceled() {
        return this.status == TransactionStatus.CANCELLED;
    }

    public boolean isReversed() {
        return this.status == TransactionStatus.REVERSED;
    }

    public boolean isScheduled() {
        return this.status == TransactionStatus.SCHEDULED;
    }

    public boolean canBeUpdated() {
        return !this.isCanceled() && !this.isReconciled() && !this.isReversed();
    }


    public boolean CanBeDeleted() {
        return this.isCanceled() || this.isReconciled() || this.isReversed();
    }
}


