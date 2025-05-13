package manager;

import model.Account;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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
}
