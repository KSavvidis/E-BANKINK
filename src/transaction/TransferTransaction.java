package transaction;

import manager.TransactionManager;
import model.Account;

import java.util.Scanner;

public class TransferTransaction implements Transaction{
    private final TransactionManager transactionManager;

    public TransferTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public void execute(Account account, Scanner sc) {
        transactionManager.performTransfer(account, sc);
    }
}
