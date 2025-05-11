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
        System.out.printf("Deposited %.2f successfully. New balance: %.2f\n", amount, account.getBalance());
    }

    public void withdraw(String iban, Scanner sc) {
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

        account.setBalance(account.getBalance() - amount);
        updateAccountInFile(account);
        System.out.printf("Withdrew %.2f successfully. New balance: %.2f\n", amount, account.getBalance());
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
}
