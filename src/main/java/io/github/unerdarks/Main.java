package io.github.unerdarks;

import io.github.unerdarks.enums.AccountType;
import io.github.unerdarks.enums.BillType;
import io.github.unerdarks.enums.CategoryType;
import io.github.unerdarks.enums.RecurringTransactionFrequency;
import io.github.unerdarks.enums.TransactionType;
import io.github.unerdarks.models.Account;
import io.github.unerdarks.models.Bill;
import io.github.unerdarks.models.Budget;
import io.github.unerdarks.models.Category;
import io.github.unerdarks.models.CreditCard;
import io.github.unerdarks.models.FinancialGoal;
import io.github.unerdarks.models.Invoice;
import io.github.unerdarks.models.RecurringTransaction;
import io.github.unerdarks.models.Transaction;
import io.github.unerdarks.repositories.AccountRepository;
import io.github.unerdarks.repositories.BillRepository;
import io.github.unerdarks.repositories.InvoiceRepository;
import io.github.unerdarks.repositories.RecurringTransactionRepository;
import io.github.unerdarks.repositories.TransactionRepository;
import io.github.unerdarks.services.AccountService;
import io.github.unerdarks.services.BillService;
import io.github.unerdarks.services.CreditCardService;
import io.github.unerdarks.services.InstallmentPurchaseService;
import io.github.unerdarks.services.RecurringTransactionService;
import io.github.unerdarks.services.ReportService;
import io.github.unerdarks.services.TransactionService;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        AccountRepository accountRepository = new AccountRepository();
        TransactionRepository transactionRepository = new TransactionRepository();
        InvoiceRepository invoiceRepository = new InvoiceRepository();
        RecurringTransactionRepository recurringTransactionRepository = new RecurringTransactionRepository();
        BillRepository billRepository = new BillRepository();

        AccountService accountService = new AccountService(accountRepository, transactionRepository);
        TransactionService transactionService = new TransactionService(transactionRepository);
        CreditCardService creditCardService = new CreditCardService(transactionRepository, transactionService, invoiceRepository);
        InstallmentPurchaseService installmentPurchaseService = new InstallmentPurchaseService(transactionService);
        RecurringTransactionService recurringTransactionService = new RecurringTransactionService(recurringTransactionRepository, transactionService);
        BillService billService = new BillService(billRepository);
        ReportService reportService = new ReportService();

        Category salary = Category.create("Salário", CategoryType.INCOME, null);
        Category food = Category.create("Alimentação", CategoryType.EXPENSE, null);
        Category.create("Restaurantes", CategoryType.EXPENSE, food);

        Account checking = accountRepository.save(Account.create("Conta Corrente", AccountType.CHECKING, new BigDecimal("2000.00"), true, BigDecimal.ZERO));
        Account savings = accountRepository.save(Account.create("Poupança", AccountType.SAVINGS, new BigDecimal("500.00"), true, BigDecimal.ZERO));

        Instant now = Instant.now();

        System.out.println("=== Categoria de despesa não pode ser usada em transação de receita ===");
        transactionService.create(Transaction.create(checking, salary, new BigDecimal("3000.00"), TransactionType.INCOME, now, "Salário de setembro"));
        try {
            Transaction.create(checking, food, new BigDecimal("50.00"), TransactionType.INCOME, now, "Tentativa inválida");
        } catch (IllegalArgumentException e) {
            System.out.println("Bloqueado como esperado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Subcategoria só pode ter o mesmo tipo da categoria pai ===");
        try {
            Category.create("Investimentos", CategoryType.INCOME, food);
        } catch (IllegalArgumentException e) {
            System.out.println("Bloqueado como esperado: " + e.getMessage());
        }

        System.out.println();
        System.out.println("=== Compra parcelada: gera todas as parcelas, mas só ativa a que vence hoje ===");
        var installmentPurchase = installmentPurchaseService.purchase(checking, food, new BigDecimal("100.00"), 3, now, "Notebook");
        installmentPurchase.getInstallments().forEach(installment ->
                System.out.printf("Parcela gerada: R$ %s - vencimento %s - status %s%n", installment.getAmount(), installment.getDate(), installment.getStatus()));

        List<Transaction> activatedInstallments = transactionService.activateDueInstallments(now);
        System.out.println("Parcelas ativadas nesta execução: " + activatedInstallments.size());
        installmentPurchase.getInstallments().forEach(installment ->
                System.out.printf("Parcela após verificação: R$ %s - status %s%n", installment.getAmount(), installment.getStatus()));

        System.out.println();
        System.out.println("=== Fechamento de fatura: compra após o fechamento vai para a fatura seguinte ===");
        Account creditAccount = accountRepository.save(Account.create("Cartão XP", AccountType.CREDIT, BigDecimal.ZERO, true, BigDecimal.ZERO));
        CreditCard creditCard = CreditCard.create(creditAccount, new BigDecimal("5000.00"), now.plus(17, ChronoUnit.DAYS), now.plus(10, ChronoUnit.DAYS));

        creditCardService.purchase(creditCard, food, new BigDecimal("150.00"), "Supermercado", now);
        Invoice closedInvoice = creditCardService.closeInvoice(creditCard);
        creditCardService.purchase(creditCard, food, new BigDecimal("80.00"), "Farmácia", now);
        Invoice nextInvoice = creditCardService.getCurrentInvoice(creditCard);

        System.out.println("Total da fatura fechada: R$ " + closedInvoice.getTotal());
        System.out.println("Total da fatura seguinte: R$ " + nextInvoice.getTotal());

        System.out.println();
        System.out.println("=== Estourar orçamento gera alerta, mas não bloqueia a despesa ===");
        Budget foodBudget = Budget.create(food, YearMonth.now(), new BigDecimal("300.00"));
        foodBudget.addExpense(new BigDecimal("350.00"));
        System.out.println("Gasto lançado normalmente: R$ " + foodBudget.getAmountSpent());
        System.out.println("Orçamento estourado? " + foodBudget.isOverBudget());
        System.out.println("Valor excedente (alerta): R$ " + foodBudget.getOverageAmount());

        System.out.println();
        System.out.println("=== Meta financeira nunca ultrapassa o valor alvo ===");
        FinancialGoal goal = FinancialGoal.create("Viagem", new BigDecimal("1000.00"), BigDecimal.ZERO, now.plus(180, ChronoUnit.DAYS));
        goal.addContribution(new BigDecimal("700.00"));
        goal.addContribution(new BigDecimal("700.00"));
        System.out.println("Valor atual da meta (nunca deve passar de 1000.00): R$ " + goal.getCurrentAmount());

        System.out.println();
        System.out.println("=== Conta a pagar/receber atrasa automaticamente ===");
        Bill overdueBill = billRepository.save(Bill.create(new BigDecimal("120.00"), now.minus(5, ChronoUnit.DAYS), BillType.TO_PAY));
        Bill upcomingBill = billRepository.save(Bill.create(new BigDecimal("200.00"), now.plus(5, ChronoUnit.DAYS), BillType.TO_RECEIVE));
        billService.refreshOverdueBills(now);
        System.out.println("Conta vencida sem pagamento: " + overdueBill.getStatus());
        System.out.println("Conta com vencimento futuro: " + upcomingBill.getStatus());
        upcomingBill.pay(now);
        System.out.println("Conta futura após ser paga: " + upcomingBill.getStatus());

        System.out.println();
        System.out.println("=== Transação recorrente não duplica a mesma competência ===");
        Transaction subscriptionTemplate = Transaction.create(checking, food, new BigDecimal("39.90"), TransactionType.EXPENSE, now, "Streaming");
        RecurringTransaction subscription = recurringTransactionRepository.save(
                RecurringTransaction.create(new BigDecimal("39.90"), RecurringTransactionFrequency.MONTHLY, now, subscriptionTemplate)
        );
        List<Transaction> firstRun = recurringTransactionService.processDueOccurrences(now);
        List<Transaction> secondRun = recurringTransactionService.processDueOccurrences(now);
        System.out.println("Ocorrências geradas na 1ª execução: " + firstRun.size());
        System.out.println("Ocorrências geradas na 2ª execução, mesma competência: " + secondRun.size());

        System.out.println();
        System.out.println("=== Transferência é neutra nos relatórios de categoria e no fluxo de caixa ===");
        accountService.transfer(checking.getId(), savings.getId(), new BigDecimal("100.00"));
        transactionService.create(Transaction.create(checking, salary, new BigDecimal("100.00"), TransactionType.TRANSFER, now, "Transferência para poupança"));

        List<Transaction> allTransactions = transactionRepository.findAll();
        System.out.println("Total por categoria (transferências excluídas): " + reportService.totalByCategory(allTransactions));
        System.out.println("Fluxo de caixa líquido (transferências excluídas): R$ " + reportService.netCashFlow(allTransactions));

        System.out.println();
        System.out.println("=== Conta com transações vinculadas não pode ser excluída ===");
        try {
            accountService.delete(checking.getId());
        } catch (IllegalArgumentException e) {
            System.out.println("Bloqueado como esperado: " + e.getMessage());
        }
        checking.desactive();
        System.out.println("Conta inativada com sucesso? " + !checking.isActive());
    }
}
