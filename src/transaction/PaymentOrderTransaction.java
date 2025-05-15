package transaction;

import manager.TransactionManager;
import model.Account;

import java.util.Scanner;

public class PaymentOrderTransaction implements Transaction {
    private final TransactionManager transactionManager;

    public PaymentOrderTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Account account, Scanner sc) {
        transactionManager.performPaymentOrder(account, sc);
    }
}
