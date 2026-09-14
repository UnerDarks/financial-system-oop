package io.github.unerdarks.models;

import io.github.unerdarks.enums.RecurringTransactionFrequency;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RecurringTransaction {
    private UUID id;
    private BigDecimal amount;
    private RecurringTransactionFrequency frequency;
    private Instant nextDateExecution;
    private Transaction transaction;
    private Set<YearMonth> generatedCompetences;

    private RecurringTransaction() {
        this.id = UUID.randomUUID();
        this.generatedCompetences = new HashSet<>();
    }

    public static RecurringTransaction create(BigDecimal amount, RecurringTransactionFrequency frequency, Instant nextDateExecution, Transaction transaction) {
        RecurringTransaction recurringTransaction = new RecurringTransaction();

        recurringTransaction.amount = amount;
        recurringTransaction.frequency = frequency;
        recurringTransaction.nextDateExecution = nextDateExecution;
        recurringTransaction.transaction = transaction;

        recurringTransaction.validate();

        return recurringTransaction;
    }

    private void validate() {
        if (amount == null || frequency == null || nextDateExecution == null || transaction == null) {
            throw new IllegalArgumentException("All parameters are required");
        }
    }

    public boolean isDue(Instant referenceDate) {
        return !nextDateExecution.isAfter(referenceDate) && !wasGeneratedFor(competenceOf(nextDateExecution));
    }

    public boolean wasGeneratedFor(YearMonth competence) {
        return generatedCompetences.contains(competence);
    }

    public Transaction generateOccurrence() {
        YearMonth competence = competenceOf(nextDateExecution);

        if (wasGeneratedFor(competence)) {
            throw new IllegalStateException("An occurrence was already generated for competence " + competence);
        }

        Transaction occurrence = Transaction.create(
                transaction.getAccount(),
                transaction.getCategory(),
                amount,
                transaction.getType(),
                nextDateExecution,
                transaction.getDescription()
        );

        generatedCompetences.add(competence);
        nextDateExecution = advance(nextDateExecution);

        return occurrence;
    }

    private YearMonth competenceOf(Instant date) {
        return YearMonth.from(date.atZone(ZoneId.systemDefault()));
    }

    private Instant advance(Instant date) {
        ZonedDateTime zonedDate = date.atZone(ZoneId.systemDefault());

        ZonedDateTime next = switch (frequency) {
            case WEEKLY -> zonedDate.plusWeeks(1);
            case MONTHLY -> zonedDate.plusMonths(1);
            case QUARTERLY -> zonedDate.plusMonths(3);
            case YEARLY -> zonedDate.plusYears(1);
        };

        return next.toInstant();
    }

    public UUID getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public RecurringTransactionFrequency getFrequency() {
        return frequency;
    }

    public Instant getNextDateExecution() {
        return nextDateExecution;
    }

    public Transaction getTransaction() {
        return transaction;
    }
}
