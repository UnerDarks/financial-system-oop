package io.github.unerdarks.models;

import io.github.unerdarks.enums.TransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class CreditCard {
    private UUID id;
    private Account account;
    private BigDecimal creditLimit;
    private Instant dueDay;
    private Instant closingDay;

    private CreditCard() {
        this.id = UUID.randomUUID();
    }

    public static CreditCard create(Account account, BigDecimal creditLimit, Instant dueDay, Instant closingDay) {
        final CreditCard creditCard = new CreditCard();

        creditCard.account = account;
        creditCard.creditLimit = creditLimit;
        creditCard.dueDay = dueDay;
        creditCard.closingDay = closingDay;

        creditCard.validate();

        return creditCard;
    }

    private void validate(){
        if(creditLimit == null || creditLimit.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("Credit limit cannot be null or non-positive");
        }
        if(dueDay == null){
            throw new IllegalArgumentException("Due day cannot be null");
        }
        if(closingDay == null){
            throw new IllegalArgumentException("Closing day cannot be null");
        }
        if(account == null){
            throw new IllegalArgumentException("Account cannot be null");
        }
    }

    public BigDecimal getUsedLimit(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getAccount().equals(account))
                .filter(transaction ->  transaction.getStatus() == (TransactionStatus.PENDING))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getAvailableLimit(List<Transaction> transactions) {
        return creditLimit.subtract(getUsedLimit(transactions));
    }

    public boolean hasAvailableLimit(BigDecimal amount, List<Transaction> transactions) {
        return amount.compareTo(getAvailableLimit(transactions)) <= 0;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public Instant getDueDay() {
        return dueDay;
    }

    public Instant getClosingDay() {
        return closingDay;
    }
}
