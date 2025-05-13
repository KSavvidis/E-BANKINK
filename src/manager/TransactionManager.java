package manager;

import model.Account;
import storage.FileStorageManager;

import java.io.*;
import java.util.*;

public class TransactionManager {
    private final AccountManager accountManager;
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";

    public TransactionManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void deposit(String iban, Scanner sc) {
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

    public void withdraw(String iban, Scanner sc) {
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

    public void transfer(String senderIban, Scanner sc) {
        //idio opws deposit
        Account sender = accountManager.findByIban(senderIban);
        if (sender == null) {
            System.out.println("Sender account not found.");
            return;
        }

        System.out.print("Enter recipient IBAN: ");
        String recipientIban = sc.nextLine();

        if (senderIban.equals(recipientIban)) {
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

        if (sender.getBalance() < amount) {
            System.out.println("Insufficient balance.");
            return;
        }

        System.out.print("Enter transfer reason: ");
        String reason = sc.nextLine();

        sender.setBalance(sender.getBalance() - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        updateAccountInFile(sender);
        updateAccountInFile(recipient);

        recordTransaction(sender, "Transfer to " + recipientIban + " - " + reason, -amount);
        recordTransaction(recipient, "Transfer from " + senderIban + " - " + reason, amount);

        System.out.printf("Transferred %.2f successfully to %s.\n", amount, recipientIban);
        System.out.printf("Sender new balance: %.2f\n", sender.getBalance());
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
                java.time.LocalDate.now(),  // Ημερομηνία
                type,                     // Τύπος συναλλαγής
                amount,                    // Ποσό
                account.getBalance()       // Υπόλοιπο
        );



        // Καταγραφή της συναλλαγής
        String statementPath = "data/statements/" + account.getIban() + ".csv";
        try (FileWriter fw = new FileWriter(statementPath, true)) {
            fw.write(transactionRecord);
        } catch (IOException e) {
            System.out.println("Error recording transaction: " + e.getMessage());
        }
    }
}
