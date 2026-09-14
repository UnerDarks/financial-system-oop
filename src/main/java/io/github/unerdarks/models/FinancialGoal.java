package io.github.unerdarks.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class FinancialGoal {
    private UUID id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private Instant targetDate;

    private FinancialGoal() {
        this.id = UUID.randomUUID();
    }

    public static FinancialGoal create(String name, BigDecimal targetAmount, BigDecimal currentAmount, Instant targetDate) {
        FinancialGoal financialGoal = new FinancialGoal();

        financialGoal.name = name;
        financialGoal.targetAmount = targetAmount;
        financialGoal.currentAmount = currentAmount;
        financialGoal.targetDate = targetDate;

        financialGoal.validate();

        return financialGoal;
    }

    private void validate() {
        if (name == null || name.trim().isBlank()) {
            throw new IllegalArgumentException("Goal name cannot be null or empty");
        }
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Target amount cannot be null or negative");
        }
        if (currentAmount == null || currentAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Current amount cannot be null or negative");
        }
        if (targetDate == null) {
            throw new IllegalArgumentException("Target date cannot be null");
        }

        if (currentAmount.compareTo(targetAmount) > 0) {
            currentAmount = targetAmount;
        }
    }

    public void addContribution(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Contribution amount must be positive");
        }

        currentAmount = currentAmount.add(amount).min(targetAmount);
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }

    public Instant getTargetDate() {
        return targetDate;
    }
}
