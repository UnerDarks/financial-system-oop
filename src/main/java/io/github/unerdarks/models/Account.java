package io.github.unerdarks.models;

import io.github.unerdarks.enums.AccountType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Account {
    private UUID id;
    private String name;
    private AccountType type;
    private BigDecimal balance;
    private Instant createdAt;
    private boolean isActive;
    private BigDecimal overdraftLimit;
    private boolean overdraftEnabled;

    private Account() {
        this.id = UUID.randomUUID();
    }

    public static Account create(String name, AccountType type, BigDecimal balance, boolean isActive, BigDecimal overdraftLimit) {
        final Account account = new Account();

        account.name = name;
        account.type = type;
        account.balance = balance;
        account.overdraftLimit = overdraftLimit;
        account.overdraftEnabled = overdraftLimit != null && overdraftLimit.compareTo(BigDecimal.ZERO) > 0;
        account.createdAt = Instant.now();
        account.isActive = isActive;

        account.validate();

        return account;
    }

    private void validate() {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }
        if (balance == null || balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Balance cannot be null or negative");
        }
        if (overdraftLimit == null || overdraftLimit.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Overdraft Limit cannot be null or negative");
        }
        if (overdraftEnabled && overdraftLimit.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be zero when overdraft is enabled");
        }
        if (!overdraftEnabled && overdraftLimit.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalArgumentException("Overdraft limit cannot be positive when overdraft is disabled");
        }
    }

    public void withdraw(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        if (balance.subtract(amount).compareTo(overdraftLimit) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        balance = balance.subtract(amount);
    }

    public void deposit(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount cannot be null or negative");
        }
        balance = balance.add(amount);
    }

    public void active() {
        if (isActive) {
          throw new IllegalArgumentException("Account is already active");
        }

        this.isActive = true;
    }

    public void desactive() {
        if (!isActive) {
          throw new IllegalArgumentException("Account is already inactive");
        }

        this.isActive = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public boolean isActive() {
        return isActive;
    }
}
