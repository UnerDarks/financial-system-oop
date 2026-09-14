package io.github.unerdarks.repositories;

import io.github.unerdarks.models.Invoice;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class InvoiceRepository {

    private final List<Invoice> invoices = new ArrayList<>();

    public Invoice save(Invoice invoice) {
        Optional<Invoice> existing = findById(invoice.getId());
        existing.ifPresent(invoices::remove);

        invoices.add(invoice);
        return invoice;
    }

    public Optional<Invoice> findById(UUID id) {
        return invoices.stream()
                .filter(invoice -> invoice.getId().equals(id))
                .findFirst();
    }

    public Optional<Invoice> findOpenByCreditCard(UUID creditCardId) {
        return invoices.stream()
                .filter(invoice -> invoice.getCreditCard().getId().equals(creditCardId))
                .filter(Invoice::isOpen)
                .findFirst();
    }

    public List<Invoice> findAllByCreditCard(UUID creditCardId) {
        return invoices.stream()
                .filter(invoice -> invoice.getCreditCard().getId().equals(creditCardId))
                .toList();
    }

    public List<Invoice> findAll() {
        return List.copyOf(invoices);
    }
}
