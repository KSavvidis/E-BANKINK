package transaction;

import manager.TransactionManager;
import model.Account;
import model.User;

import java.util.Scanner;

public class PaymentOrderTransaction implements ScheduledTransaction {
    private final TransactionManager transactionManager;

    public PaymentOrderTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Account account, Scanner sc) {

    }

    @Override
    public boolean execute() {
        return false;
    }
}
