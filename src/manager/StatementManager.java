package manager;

import model.Account;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class StatementManager {



    public void initializeStatementFiles(List<Account> accounts) {
        File folder = new File("data/statements");




        for (Account account : accounts) {
            String iban = account.getIban();
            File file = new File(folder, iban + ".csv");

            try {
                if (!file.exists()) {
                    file.createNewFile();


                }
            } catch (IOException e) {
                System.out.println("Error creating statement file for IBAN: " + iban);
            }
        }
    }

    public void recordTransaction(Account account, String type, double amount) {
        //na baloume to currentDate san parametro kai nallajoume oles tis transaction manager
        String transactionRecord = String.format("%s,%s,%.2f,%.2f\n",
                LocalDate.now(),
                type,
                amount,
                account.getBalance()
        );

        String statementPath = "data/statements/" + account.getIban() + ".csv";

        try {

            String existingContent = "";
            File file = new File(statementPath);
            if (file.exists()) {
                existingContent = new String(Files.readAllBytes(file.toPath()));
            }


            try (FileWriter fw = new FileWriter(statementPath)) {
                fw.write(transactionRecord + existingContent);
            }
        } catch (IOException e) {
            System.out.println("Error recording transaction: " + e.getMessage());
        }
    }
}