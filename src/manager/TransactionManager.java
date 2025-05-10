package manager;

import model.Account;
import storage.FileStorageManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionManager {
    private final AccountManager accountManager;
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";

    public TransactionManager(AccountManager accountManager) {
        this.accountManager = accountManager;
    }

    public void deposit(String vat, double amount) {
        //vriskei ton logariasmo sto account.csv me vasi to vat tou user.csv
        Account acc = accountManager.findByVat(vat);
        if (acc != null) {
            //enimerwnei to upoloipo
            acc.setBalance(acc.getBalance() + amount);
            //enimerwnei to arxeio gia tin allagi tou ypoloipou
            updateAccountInFile(acc);
            System.out.println("Deposit completed. New balance: " + acc.getBalance());
        } else {
            System.out.println("Account not found.");
        }
    }

    public void withdraw(String vat, double amount) {
        //vriskei ton logariasmo sto account.csv me vasi to vat tou user.csv
        Account acc = accountManager.findByVat(vat);
        if (acc != null) {
            //elegxei tin diathesimotita twn xrimatwn
            if (acc.getBalance() >= amount) {
                //enimerwsi upoloipou
                acc.setBalance(acc.getBalance() - amount);
                //enimerwsi arxeiou
                updateAccountInFile(acc);
                System.out.println("Withdrawal completed. New balance: " + acc.getBalance());
            } else {
                System.out.println("Insufficient funds.");
            }
        } else {
            System.out.println("Account not found.");
        }
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
