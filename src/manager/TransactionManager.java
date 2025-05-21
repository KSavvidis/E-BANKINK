package manager;

import model.Account;
import model.BankAccount;
import storage.FileStorageManager;
import model.Bill;

import java.io.*;
import java.util.*;

public class TransactionManager {
    private  AccountManager accountManager;
    private  BillManager billManager;

    private final FileStorageManager storageManager = new FileStorageManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";

    public TransactionManager(AccountManager accountManager, BillManager billManager) {
        this.accountManager = accountManager;
        this.billManager = billManager;
    }

    public TransactionManager() {
    }

    public void performDeposit(String iban, Scanner sc) {
        Account account = accountManager.findByIban(iban);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter amount to deposit: ");
        double amount = sc.nextDouble();
        sc.nextLine(); // Consume newline

        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        account.setBalance(account.getBalance() + amount);
        updateAccountInFile(account);

        recordTransaction(account, "ATM Deposit", amount);

        System.out.printf("Deposited %.2f successfully. New balance: %.2f\n", amount, account.getBalance());
    }

    public void performWithdraw(String iban, Scanner sc) {
        Account account = accountManager.findByIban(iban);
        if (account == null) {
            System.out.println("Account not found.");
            return;
        }

        System.out.print("Enter amount to withdraw: ");
        double amount = sc.nextDouble();
        sc.nextLine(); // Consume newline

        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        account.setBalance(account.getBalance() - amount);
        updateAccountInFile(account);

        recordTransaction(account, "ATM Withdraw", amount);

        System.out.printf("Withdrew %.2f successfully. New balance: %.2f\n", amount, account.getBalance());
    }

    public void performTransfer(Account senderAccount, Scanner sc) {
        //idio opws deposit

        if (senderAccount == null) {
            System.out.println("Sender account not found.");
            return;
        }

        System.out.print("Enter recipient IBAN: ");
        String recipientIban = sc.nextLine();

        if (senderAccount.getIban().equals(recipientIban)) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        Account recipient = accountManager.findByIban(recipientIban);
        if (recipient == null) {
            System.out.println("Recipient account not found.");
            return;
        }

        System.out.print("Enter amount to transfer: ");
        double amount = sc.nextDouble();
        sc.nextLine();


        if (amount <= 0) {
            System.out.println("Amount must be positive.");
            return;
        }

        if (senderAccount.getBalance() < amount) {
            System.out.println("Insufficient balance.");
            return;
        }

        System.out.print("Enter transfer reason: ");
        String reason = sc.nextLine();

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        updateAccountInFile(senderAccount);
        updateAccountInFile(recipient);

        recordTransaction(senderAccount, "Transfer to " + recipientIban + " - " + reason, -amount);
        recordTransaction(recipient, "Transfer from " + senderAccount.getIban() + " - " + reason, amount);

        System.out.printf("Transferred %.2f successfully to %s.\n", amount, recipientIban);
        System.out.printf("Sender new balance: %.2f\n", senderAccount.getBalance());
    }

    public void performOrderTransfer(Account senderAccount, Account receiverAccount, double amount, String description) {
        if (senderAccount == null) {
            System.out.println("Sender account not found.");
            return;
        }

        if (receiverAccount == null) {
            System.out.println("Recipient account not found.");
            return;
        }

        if (senderAccount.getIban().equals(receiverAccount.getIban())) {
            System.out.println("Cannot transfer to the same account.");
            return;
        }

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        receiverAccount.setBalance(receiverAccount.getBalance() + amount);

        updateAccountInFile(senderAccount);
        updateAccountInFile(receiverAccount);

        recordTransaction(senderAccount, "Transfer to " + receiverAccount.getIban() + " - " + description, -amount);
        recordTransaction(receiverAccount, "Transfer from " + senderAccount.getIban() + " - " + description, amount);

        System.out.printf("Transferred %.2f successfully to %s.\n", amount, receiverAccount.getIban());
        System.out.printf("Sender new balance: %.2f\n", senderAccount.getBalance());
    }

    public void performPayment(String iban, String vat, Scanner sc) {

        List<Bill> pendingBills = billManager.getBillsForCustomer(vat);

        if (pendingBills.isEmpty()) {
            System.out.println("You have no bills to pay.");
            return;
        }

        System.out.println("Pending bills:");
        int i = 0;
        for (Bill bill: pendingBills) {
            System.out.printf("%d. RF: %s | Amount: %.2f | Due: %s | Issuer: %s\n",
                    i + 1, bill.getPaymentCode(), bill.getAmount(), bill.getDueDate(), bill.getIssuer().getVAT());
            i++;
        }

        System.out.print("Select bill by RF: ");
        String rf = sc.next();
        sc.nextLine();
        Bill selectedBill = null;
        for(Bill bill: pendingBills) {
            if(rf.equals(bill.getPaymentCode())) {
                selectedBill = bill;
            }
        }
        if(selectedBill == null) {
            System.out.println("Bill not found.");
            return;
        }
        Account selectedAccount = accountManager.findByIban(iban);
        if (selectedAccount == null) {
            System.out.println("No account selected. Aborting payment.");
            return;
        }

        if (selectedAccount.getBalance() < selectedBill.getAmount()) {
            System.out.println("Insufficient funds in the selected account.");
            return;
        }

        selectedAccount.setBalance(selectedAccount.getBalance() - selectedBill.getAmount());
        System.out.printf("Payment of %.2f for bill %s completed. Updated account balance: %.2f\n",
                selectedBill.getAmount(), selectedBill.getPaymentCode(), selectedAccount.getBalance());


        updateAccountInFile(selectedAccount);


        recordTransaction(selectedAccount, "Bill Payment", -selectedBill.getAmount());
    }

    public void performOrderTransferFee(Account senderAccount, double amount, String description) {
        BankAccount bankAccount = BankAccount.getInstance();
        if (senderAccount == null) {
            System.out.println("Sender account not found.");
            return;
        }
        if(bankAccount == null) {
            System.out.println("Bank's account not found.");
            return;
        }

        senderAccount.setBalance(senderAccount.getBalance() - amount);
        bankAccount.setBalance(bankAccount.getBalance() + amount);
        updateAccountInFile(senderAccount);
        updateAccountInFile(bankAccount);
        recordTransaction(senderAccount, "Transfered fee to Bank: " + amount, -amount);
        recordTransaction(bankAccount, "Transfered fee from " + senderAccount.getIban() + " - " + description, + amount);
        System.out.printf("Sender new balance: %.2f\n", senderAccount.getBalance());
    }

    // enimerwsi tis grammis tou arxeiou mono tou account pou ekane login
    private void updateAccountInFile(Account updatedAccount) {
        List<String> updatedLines = new ArrayList<>();

        //diavazei olo to arxeio
        try (BufferedReader reader = new BufferedReader(new FileReader(accountsFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                //allazei mono tin grammi tou logariasmou pou ekane login
                if (line.contains("iban:" + updatedAccount.getIban())) {
                    updatedLines.add(updatedAccount.marshal());
                } else {
                    updatedLines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        //eggrafi olwn twn grammwn pali sto arxeio
        try (PrintWriter writer = new PrintWriter(accountsFilePath)) {
            for (String l : updatedLines) {
                writer.println(l);
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    private void recordTransaction(Account account, String type, double amount) {
        String transactionRecord = String.format("%s,%s,%.2f,%.2f\n",
                java.time.LocalDate.now(),
                type,                     //typos synalagis
                amount,
                account.getBalance()
        );

        String statementPath = "data/statements/" + account.getIban() + ".csv";
        try (FileWriter fw = new FileWriter(statementPath, true)) {
            fw.write(transactionRecord);
        } catch (IOException e) {
            System.out.println("Error recording transaction: " + e.getMessage());
        }
    }
}
