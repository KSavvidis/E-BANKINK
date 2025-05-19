package manager;

import model.Bill;
import storage.FileStorageManager;
import storage.Storable;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

public class BillManager {
    private final List<Bill> bills = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();

    private final String billsFolderPath = "data/bills/";
    private final String issuedFilePath = billsFolderPath + "issued.csv";
    private final String expiredFilePath = billsFolderPath + "expired.csv";

    public BillManager() {

        loadBillsForToday();
    }

    public void loadBillsForToday() {
        loadBillsForDate(LocalDate.now());
        simulateForExpiry(LocalDate.now());
    }

    public void loadBillsForDate(LocalDate date) {
        String filename = date.toString() + ".csv";
        File billFile = new File(billsFolderPath + filename);

        if (!billFile.exists()) {
            System.out.println("No bills found for today.");
            return;
        }

        List<String> billIssued = new ArrayList<>();
        List<String> billExpired = new ArrayList<>();

        storageManager.load(new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                try {
                    Bill bill = parseBill(line);
                    if (bill != null) {
                        bills.add(bill);
                        LocalDate dueDate = LocalDate.parse(bill.getDueDate());
                        if (dueDate.isBefore(date)) {
                            if (!isLineInFile(line, expiredFilePath)) {
                                billExpired.add(line);
                            }
                        } else {
                            if (!isLineInFile(line, issuedFilePath)) {
                                billIssued.add(line);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        }, billFile.getPath());

        appendToFile(issuedFilePath, billIssued);
        appendToFile(expiredFilePath, billExpired);
    }

    private Bill parseBill(String line) {
        Map<String, String> billFields = new HashMap<>();
        String[] fields = line.split(",");

        for (String field : fields) {
            String[] keyValue = field.split(":", 2);
            if (keyValue.length == 2) {
                billFields.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return new Bill(
                billFields.get("type"),
                billFields.get("paymentCode"),
                billFields.get("billNumber"),
                billFields.get("issuer"),
                billFields.get("customer"),
                Double.parseDouble(billFields.get("amount")),
                billFields.get("issueDate"),
                billFields.get("dueDate")
        );
    }

    private boolean isLineInFile(String line, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.equals(line)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.out.println("Error checking file: " + e.getMessage());
        }
        return false;
    }

    private void appendToFile(String filePath, List<String> lines) {
        if (lines.isEmpty()) return;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    protected void simulateForExpiry(LocalDate currentDate) {
        List<String> activeBills = new ArrayList<>();
        List<String> expiredBills = new ArrayList<>();

        // Read current issued bills
        try (BufferedReader reader = new BufferedReader(new FileReader(issuedFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Bill bill = parseBill(line);
                if (bill != null) {
                    LocalDate dueDate = LocalDate.parse(bill.getDueDate());
                    if (dueDate.isBefore(currentDate)) {
                        if (!isLineInFile(line, expiredFilePath)) {
                            expiredBills.add(line);
                        }
                    } else {
                        activeBills.add(line);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading issued bills: " + e.getMessage());
        }

        // Update files
        if (!expiredBills.isEmpty()) {
            appendToFile(expiredFilePath, expiredBills);
            writeFile(issuedFilePath, activeBills);
        }
    }

    private void writeFile(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing file: " + e.getMessage());
        }
    }

    protected List<Bill> getBillsForCustomer(String vat) {
        List<Bill> result = new ArrayList<>();

        storageManager.load(new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                Bill bill = parseBill(line);
                if (bill != null && bill.getCustomer().equals(vat)) {
                    result.add(bill);
                }
            }
        }, issuedFilePath);

        return result;
    }

    public void manualLoadBillsByDate(Scanner scanner, String vat) {
        System.out.println("\nLoad issued bills by date");
        System.out.println("===================================");
        System.out.print("Enter date to load from (YYYY-MM-DD): ");
        String dateInput = scanner.nextLine().trim();

        try {
            LocalDate date = LocalDate.parse(dateInput);
            String filename = date.toString() + ".csv";
            File billFile = new File(billsFolderPath + filename);



            if (!billFile.exists()) {
                System.out.println(filename+".csv file does not exist");
                return;
            }

            List<String> billsToAdd = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(billFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Bill bill = parseBill(line);
                    if (bill != null && bill.getIssuer().equals(vat)) {
                        String billLine = bill.marshal();
                        if (!isLineInFile(billLine, issuedFilePath) &&
                                !isLineInFile(billLine, expiredFilePath)) {
                            billsToAdd.add(billLine);
                        }
                    }
                }
            }

            if (billsToAdd.isEmpty()) {
                System.out.println("There are not new bills for this: " + date+"date");
            } else {
                appendToFile(issuedFilePath, billsToAdd);
                System.out.println("added " + billsToAdd.size() + " bills to file:");
                for (String billLine : billsToAdd) {
                    System.out.println("- " + billLine);
                }
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());

        }

        System.out.println("\nPress enter to continue...");
        scanner.nextLine();
    }

   }