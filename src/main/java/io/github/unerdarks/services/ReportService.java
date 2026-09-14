package io.github.unerdarks.services;

import io.github.unerdarks.enums.TransactionType;
import io.github.unerdarks.models.Category;
import io.github.unerdarks.models.Transaction;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {

    public Map<Category, BigDecimal> totalByCategory(List<Transaction> transactions) {
        Map<Category, BigDecimal> totals = new HashMap<>();

        transactions.stream()
                .filter(transaction -> transaction.getType() != TransactionType.TRANSFER)
                .forEach(transaction -> totals.merge(transaction.getCategory(), transaction.getAmount(), BigDecimal::add));

        return totals;
    }

    public BigDecimal netCashFlow(List<Transaction> transactions) {
        return transactions.stream()
                .filter(transaction -> transaction.getType() != TransactionType.TRANSFER)
                .map(transaction -> transaction.getType() == TransactionType.INCOME
                        ? transaction.getAmount()
                        : transaction.getAmount().negate())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
