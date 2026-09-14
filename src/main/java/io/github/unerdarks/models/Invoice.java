package io.github.unerdarks.models;

import io.github.unerdarks.enums.InvoiceStatus;
import io.github.unerdarks.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Invoice {
    private UUID id;
    private CreditCard creditCard;
    private Instant closingDate;
    private Instant dueDate;
    private InvoiceStatus status;
    private List<Transaction> transactions;

    private Invoice() {
        this.id = UUID.randomUUID();
        this.transactions = new ArrayList<>();
    }

    public static Invoice open(CreditCard creditCard, Instant closingDate, Instant dueDate) {
        Invoice invoice = new Invoice();

        invoice.creditCard = creditCard;
        invoice.closingDate = closingDate;
        invoice.dueDate = dueDate;
        invoice.status = InvoiceStatus.OPEN;

        invoice.validate();

        return invoice;
    }

    private void validate() {
        if (creditCard == null) {
            throw new IllegalArgumentException("Credit card cannot be null");
        }
        if (closingDate == null) {
            throw new IllegalArgumentException("Closing date cannot be null");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null");
        }
    }

    public boolean accepts(Instant purchaseDate) {
        return isOpen() && !purchaseDate.isAfter(closingDate);
    }

    public void addTransaction(Transaction transaction) {
        if (!accepts(transaction.getDate())) {
            throw new IllegalArgumentException("Transaction does not belong to this invoice");
        }

        transactions.add(transaction);
    }

    public void close() {
        if (!isOpen()) {
            throw new IllegalStateException("Invoice is already closed");
        }

        this.status = InvoiceStatus.CLOSED;
    }

    public boolean isOpen() {
        return this.status == InvoiceStatus.OPEN;
    }

    public BigDecimal getTotal() {
        return transactions.stream()
                .filter(transaction -> transaction.getStatus() != TransactionStatus.CANCELLED)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public UUID getId() {
        return id;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public Instant getClosingDate() {
        return closingDate;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public List<Transaction> getTransactions() {
        return List.copyOf(transactions);
    }
}
