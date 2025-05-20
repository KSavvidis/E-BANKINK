package transaction;

import manager.TransactionManager;
import model.Account;

import java.util.Scanner;

public class TransferOrderTransaction implements ScheduledTransaction {
    private final TransactionManager transactionManager;

    public TransferOrderTransaction(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public boolean execute(Account senderAccount, Account receiverAccount, double amount, String description, double fee) {
        if(senderAccount.getBalance() < amount + fee) {
            System.out.println("Insufficient funds in account with IBAN: " + senderAccount.getIban());
            return false;
        }
        double feeAmount = amount * fee/100;
        transactionManager.performOrderTransfer(senderAccount, receiverAccount, amount, description);
        transactionManager.performOrderTransferFee(senderAccount, feeAmount, description);
        return true;
    }

    @Override
    public boolean execute() {
        return false;
    }

    @Override
    public void execute(Account account, Scanner sc) {

    }
}
