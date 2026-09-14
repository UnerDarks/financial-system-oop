package io.github.unerdarks.models;

import io.github.unerdarks.enums.BillStatus;
import io.github.unerdarks.enums.BillType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class Bill {
    private UUID id;
    private BigDecimal amount;
    private Instant dueDate;
    private BillType type;
    private BillStatus status;
    private Instant paymentDate;

    private Bill() {
        this.id = UUID.randomUUID();
    }

    public static Bill create(BigDecimal amount, Instant dueDate, BillType type) {
        Bill bill = new Bill();

        bill.amount = amount;
        bill.dueDate = dueDate;
        bill.type = type;
        bill.status = BillStatus.PENDING;

        bill.validate();

        return bill;
    }

    private void validate() {
        if (amount == null || dueDate == null || type == null) {
            throw new IllegalArgumentException("Bill fields cannot be null");
        }
    }

    public void pay(Instant paymentDate) {
        if (status == BillStatus.PAID) {
            throw new IllegalStateException("Bill is already paid");
        }

        this.status = BillStatus.PAID;
        this.paymentDate = paymentDate;
    }

    public void refreshStatus(Instant referenceDate) {
        if (status == BillStatus.PENDING && referenceDate.isAfter(dueDate)) {
            this.status = BillStatus.OVERDUE;
        }
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Instant getDueDate() {
        return dueDate;
    }

    public BillType getType() {
        return type;
    }

    public BillStatus getStatus() {
        return status;
    }

    public Instant getPaymentDate() {
        return paymentDate;
    }
}
