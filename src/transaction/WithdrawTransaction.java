package transaction;

import java.util.Scanner;
import manager.AccountManager;
import manager.TransactionManager;
import model.Account;

public class WithdrawTransaction implements Transaction {
    private final TransactionManager transactionManager;

    public WithdrawTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Account account, Scanner sc) {
        transactionManager.performWithdraw(account.getIban(), sc);
    }
}
