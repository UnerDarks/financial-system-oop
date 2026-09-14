package io.github.unerdarks.models;

import io.github.unerdarks.enums.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InstallmentPurchase {
    private UUID id;
    private BigDecimal totalAmount;
    private int numberOfInstallments;
    private Instant purchaseDate;
    private Account account;
    private List<Transaction> installments;

    private InstallmentPurchase() {
        this.id = UUID.randomUUID();
    }

    public static InstallmentPurchase create(Account account, Category category, BigDecimal totalAmount, int numberOfInstallments, Instant purchaseDate, String description) {
        InstallmentPurchase purchase = new InstallmentPurchase();

        purchase.account = account;
        purchase.totalAmount = totalAmount;
        purchase.numberOfInstallments = numberOfInstallments;
        purchase.purchaseDate = purchaseDate;

        purchase.validate();

        purchase.installments = purchase.generateInstallments(category, description);

        return purchase;
    }

    private void validate() {
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }
        if (totalAmount == null || totalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Total amount must be positive");
        }
        if (numberOfInstallments <= 0) {
            throw new IllegalArgumentException("Number of installments must be positive");
        }
        if (purchaseDate == null) {
            throw new IllegalArgumentException("Purchase date cannot be null");
        }
    }

    private List<Transaction> generateInstallments(Category category, String description) {
        List<Transaction> generated = new ArrayList<>();

        BigDecimal installmentCount = BigDecimal.valueOf(numberOfInstallments);
        BigDecimal baseInstallment = totalAmount.divide(installmentCount, 2, RoundingMode.DOWN);
        BigDecimal remainder = totalAmount.subtract(baseInstallment.multiply(installmentCount));

        ZonedDateTime firstDueDate = purchaseDate.atZone(ZoneId.systemDefault());

        for (int i = 0; i < numberOfInstallments; i++) {
            BigDecimal installmentAmount = baseInstallment;
            if (i == numberOfInstallments - 1) {
                installmentAmount = installmentAmount.add(remainder);
            }

            Instant dueDate = firstDueDate.plusMonths(i).toInstant();
            String installmentDescription = describeInstallment(description, i + 1, numberOfInstallments);

            generated.add(Transaction.createScheduled(
                    account,
                    category,
                    installmentAmount,
                    TransactionType.EXPENSE,
                    dueDate,
                    installmentDescription
            ));
        }

        return generated;
    }

    private String describeInstallment(String description, int installmentNumber, int totalInstallments) {
        if (description == null || description.isBlank()) {
            return "Parcela %d/%d".formatted(installmentNumber, totalInstallments);
        }

        return "%s (%d/%d)".formatted(description, installmentNumber, totalInstallments);
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public int getNumberOfInstallments() {
        return numberOfInstallments;
    }

    public Instant getPurchaseDate() {
        return purchaseDate;
    }

    public Account getAccount() {
        return account;
    }

    public List<Transaction> getInstallments() {
        return List.copyOf(installments);
    }
}
