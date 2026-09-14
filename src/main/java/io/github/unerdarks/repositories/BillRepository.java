package io.github.unerdarks.repositories;

import io.github.unerdarks.models.Bill;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BillRepository {

    private final List<Bill> bills = new ArrayList<>();

    public Bill save(Bill bill) {
        Optional<Bill> existing = findById(bill.getId());
        existing.ifPresent(bills::remove);

        bills.add(bill);
        return bill;
    }

    public Optional<Bill> findById(UUID id) {
        return bills.stream()
                .filter(bill -> bill.getId().equals(id))
                .findFirst();
    }

    public List<Bill> findAll() {
        return List.copyOf(bills);
    }
}
