package io.github.unerdarks.services;

import io.github.unerdarks.enums.TransactionType;
import io.github.unerdarks.models.Category;
import io.github.unerdarks.models.CreditCard;
import io.github.unerdarks.models.Invoice;
import io.github.unerdarks.models.Transaction;
import io.github.unerdarks.repositories.InvoiceRepository;
import io.github.unerdarks.repositories.TransactionRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class CreditCardService {

    private final TransactionRepository transactionRepository;
    private final TransactionService transactionService;
    private final InvoiceRepository invoiceRepository;

    public CreditCardService(TransactionRepository transactionRepository, TransactionService transactionService, InvoiceRepository invoiceRepository) {
        this.transactionRepository = transactionRepository;
        this.transactionService = transactionService;
        this.invoiceRepository = invoiceRepository;
    }

    public Transaction purchase(CreditCard creditCard, Category category, BigDecimal amount, String description) {
        return purchase(creditCard, category, amount, description, Instant.now());
    }

    public Transaction purchase(CreditCard creditCard, Category category, BigDecimal amount, String description, Instant purchaseDate) {
        List<Transaction> transactions = transactionRepository.findOpenByAccount(creditCard.getAccount().getId());

        if (!creditCard.hasAvailableLimit(amount, transactions)) {
            throw new IllegalArgumentException("Insufficient credit limit");
        }

        Invoice invoice = resolveInvoiceFor(creditCard, purchaseDate);

        Transaction transaction = Transaction.create(
                creditCard.getAccount(),
                category,
                amount,
                TransactionType.EXPENSE,
                purchaseDate,
                description
        );

        invoice.addTransaction(transaction);
        invoiceRepository.save(invoice);

        return transactionService.create(transaction);
    }

    public Invoice closeInvoice(CreditCard creditCard) {
        Invoice currentInvoice = getCurrentInvoice(creditCard);

        advanceToNextInvoice(currentInvoice);

        return currentInvoice;
    }

    public Invoice getCurrentInvoice(CreditCard creditCard) {
        return invoiceRepository.findOpenByCreditCard(creditCard.getId())
                .orElseGet(() -> invoiceRepository.save(
                        Invoice.open(creditCard, creditCard.getClosingDay(), creditCard.getDueDay())
                ));
    }

    private Invoice resolveInvoiceFor(CreditCard creditCard, Instant purchaseDate) {
        Invoice invoice = getCurrentInvoice(creditCard);

        while (!invoice.accepts(purchaseDate)) {
            invoice = advanceToNextInvoice(invoice);
        }

        return invoice;
    }

    private Invoice advanceToNextInvoice(Invoice invoice) {
        if (invoice.isOpen()) {
            invoice.close();
            invoiceRepository.save(invoice);
        }

        Invoice nextInvoice = Invoice.open(
                invoice.getCreditCard(),
                invoice.getClosingDate().atZone(ZoneId.systemDefault()).plusMonths(1).toInstant(),
                invoice.getDueDate().atZone(ZoneId.systemDefault()).plusMonths(1).toInstant()
        );

        return invoiceRepository.save(nextInvoice);
    }
}
