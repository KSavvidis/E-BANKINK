package transaction;

import java.util.Scanner;
import manager.TransactionManager;
import model.Account;

public class PaymentTransaction implements Transaction {
    private final TransactionManager transactionManager;

    public PaymentTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Account account, Scanner sc) {
        transactionManager.performPayment(account.getIban(),account.getPrimaryOwner().getVAT(), sc);
    }
}
