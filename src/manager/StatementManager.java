package manager;

import model.Account;
import model.FailedOrder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.util.List;

public class StatementManager {

private final String failedOrdersFilePath = "data/orders/failed.csv";

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

    public void recordTransaction(Account account, String type, double amount, LocalDate currentDate) {
        //na baloume to currentDate san parametro kai nallajoume oles tis transaction manager
        String transactionRecord = String.format("%s,%s,%.2f,%.2f\n",
                currentDate,
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

    public void failedOrderFolder(FailedOrder failedOrder, LocalDate currentDate, double amount) {
        StringBuffer sb = new StringBuffer();
        sb.append(currentDate).append(" | ");
        sb.append("From: ").append(failedOrder.getOrder().getChargeAccount().getIban()).append(" | ");
        sb.append("Amount: ").append(amount).append(" | ");
        sb.append("Reason: ").append(failedOrder.getOrder().getDescription());

        writeToFailedFile(sb.toString());
    }

    private void writeToFailedFile(String content){
        try{
            File file = new File(failedOrdersFilePath);
            if (!file.exists()) {
                file.createNewFile();
                System.out.println("Error creating file: " + failedOrdersFilePath);
                return;
            }
            try(FileWriter writer = new FileWriter(file, true)){
                writer.write(content + "\n");
            }
        }
        catch (Exception e){
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}