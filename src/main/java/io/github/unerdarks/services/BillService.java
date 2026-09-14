package io.github.unerdarks.services;

import io.github.unerdarks.models.Bill;
import io.github.unerdarks.repositories.BillRepository;

import java.time.Instant;
import java.util.List;

public class BillService {

    private final BillRepository billRepository;

    public BillService(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    public List<Bill> refreshOverdueBills(Instant referenceDate) {
        List<Bill> bills = billRepository.findAll();

        bills.forEach(bill -> bill.refreshStatus(referenceDate));
        bills.forEach(billRepository::save);

        return bills;
    }
}
