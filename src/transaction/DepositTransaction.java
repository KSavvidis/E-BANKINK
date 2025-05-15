package transaction;

import java.util.Scanner;
import manager.AccountManager;
import manager.TransactionManager;
import model.Account;

public class DepositTransaction implements Transaction {
    private final TransactionManager transactionManager;

    public DepositTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Account account, Scanner sc) { transactionManager.performDeposit(account.getIban(), sc);
    }
}
