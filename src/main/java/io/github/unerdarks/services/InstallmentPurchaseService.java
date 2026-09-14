package io.github.unerdarks.services;

import io.github.unerdarks.models.Account;
import io.github.unerdarks.models.Category;
import io.github.unerdarks.models.InstallmentPurchase;
import io.github.unerdarks.models.Transaction;

import java.math.BigDecimal;
import java.time.Instant;

public class InstallmentPurchaseService {

    private final TransactionService transactionService;

    public InstallmentPurchaseService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public InstallmentPurchase purchase(Account account, Category category, BigDecimal totalAmount, int numberOfInstallments, Instant purchaseDate, String description) {
        InstallmentPurchase installmentPurchase = InstallmentPurchase.create(
                account, category, totalAmount, numberOfInstallments, purchaseDate, description
        );

        for (Transaction installment : installmentPurchase.getInstallments()) {
            transactionService.create(installment);
        }

        return installmentPurchase;
    }
}
