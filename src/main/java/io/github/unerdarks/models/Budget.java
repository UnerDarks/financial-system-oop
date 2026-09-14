package io.github.unerdarks.models;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;

public class Budget {
    private Category category;
    private YearMonth referenceMonth;
    private BigDecimal plannedValue;
    private BigDecimal amountSpent;

    private Budget() {}

    public static Budget create(Category category, YearMonth referenceMonth, BigDecimal plannedValue) {
        Budget budget = new Budget();

        budget.category = category;
        budget.referenceMonth = referenceMonth;
        budget.plannedValue = plannedValue;
        budget.amountSpent = BigDecimal.ZERO;

        budget.validate();

        return budget;
    }

    public void validate() {
        if (category == null || referenceMonth == null || plannedValue == null) {
            throw new IllegalArgumentException("Budget fields cannot be null");
        }
    }

    public void addExpense(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        amountSpent = amountSpent.add(amount);
    }

    public boolean isOverBudget() {
        return amountSpent.compareTo(plannedValue) > 0;
    }

    public BigDecimal getOverageAmount() {
        BigDecimal overage = amountSpent.subtract(plannedValue);
        return overage.compareTo(BigDecimal.ZERO) > 0 ? overage : BigDecimal.ZERO;
    }

    public Category getCategory() {
        return category;
    }

    public YearMonth getReferenceMonth() {
        return referenceMonth;
    }

    public BigDecimal getPlannedValue() {
        return plannedValue;
    }

    public BigDecimal getAmountSpent() {
        return amountSpent;
    }
}
