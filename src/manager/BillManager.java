package manager;

import model.Bill;
import storage.FileStorageManager;
import storage.Storable;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class BillManager {
    private final List<Bill> bills = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();

    private final String billsFolderPath = "data/bills/";
    private final String issuedFilePath = billsFolderPath + "issued.csv";

    public BillManager() {
        // Αν θέλεις να φορτώνονται μόνο με ρητή εντολή, αφαίρεσε το:
        loadBillsForToday();
    }

    public void loadBillsForToday() {
        loadBillsForDate(LocalDate.now());
    }

    public void loadBillsForDate(LocalDate date) {
        String filename = date.toString() + ".csv";
        File billFile = new File(billsFolderPath + filename);

        if (!billFile.exists()) {
            System.out.println("No bills found for: " + date);
            return;
        }

        List<String> rawLines = new ArrayList<>();

        Storable billLoader = new Storable() {
            @Override
            public String marshal() {
                return null;
            }

            @Override
            public void unmarshal(String line) {
                rawLines.add(line);

                Map<String, String> billFields = new HashMap<>();
                String[] fields = line.split(",");

                for (String field : fields) {
                    String[] keyValue = field.split(":", 2);
                    if (keyValue.length == 2) {
                        billFields.put(keyValue[0].trim(), keyValue[1].trim());
                    }
                }

                try {
                    Bill bill = new Bill(
                            billFields.get("type"),
                            billFields.get("paymentCode"),
                            billFields.get("billNumber"),
                            billFields.get("issuer"),
                            billFields.get("customer"),
                            Double.parseDouble(billFields.get("amount")),
                            billFields.get("issueDate"),
                            billFields.get("dueDate")
                    );
                    bills.add(bill);
                } catch (Exception e) {
                    System.out.println("Error parsing bill from line: " + line);
                }
            }
        };

        storageManager.load(billLoader, billFile.getPath());
        appendToIssuedCsv(rawLines);
    }

    private void appendToIssuedCsv(List<String> lines) {
        File issuedFile = new File(issuedFilePath);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(issuedFile, true))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
            System.out.println("Added " + lines.size() + " bills to issued.csv");
        } catch (IOException e) {
            System.out.println("Failed to write to issued.csv: " + e.getMessage());
        }
    }

    public List<Bill> getBillsForCustomer(String vat) {
        List<Bill> result = new ArrayList<>();
        for (Bill bill : bills) {
            if (bill.getCustomer().equals(vat)) {
                result.add(bill);
            }
        }
        return result;
    }
}
