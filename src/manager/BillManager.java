package manager;

import model.Bill;
import storage.FileStorageManager;
import storage.Storable;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class BillManager {
    private final List<Bill> bills = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();
    private final UserManager userManager = new UserManager();

    private final String billsFolderPath = "data/bills/";
    private final String issuedFilePath = billsFolderPath + "issued.csv";
    private final String expiredFilePath = billsFolderPath + "expired.csv";
    private final String paidFilePath = billsFolderPath + "paid.csv";

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
            System.out.println("No bills found for " + date);
            return;
        }

        List<String> billIssued = new ArrayList<>();
        List<String> billExpired = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(billFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Bill bill = parseBill(line);
                    if (bill != null) {
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
                    System.err.println("Error processing line: " + line);
                    System.err.println("Error details: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading bills file: " + e.getMessage());
            return;
        }

        appendToFile(issuedFilePath, billIssued);
        appendToFile(expiredFilePath, billExpired);
    }

    protected void simulateForExpiry(LocalDate currentDate) {
        List<String> activeBills = new ArrayList<>();
        List<String> expiredBills = new ArrayList<>();

        // Create files if they don't exist
        File issuedFile = new File(issuedFilePath);
        if (!issuedFile.exists()) {
            try {
                issuedFile.createNewFile();
                return;
            } catch (IOException e) {
                System.err.println("Error creating issued bills file: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(issuedFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
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
                } catch (Exception e) {
                    System.err.println("Error processing bill: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading issued bills: " + e.getMessage());
            return;
        }

        if (!expiredBills.isEmpty()) {
            appendToFile(expiredFilePath, expiredBills);
            writeFile(issuedFilePath, activeBills);
        }
    }

    private Bill parseBill(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        Map<String, String> billFields = new HashMap<>();
        String[] fields = line.split(",");

        try {
            for (String field : fields) {
                String[] keyValue = field.split(":", 2);
                if (keyValue.length == 2) {
                    billFields.put(keyValue[0].trim(), keyValue[1].trim());
                }
            }

            // Validate required fields
            if (billFields.get("amount") == null || billFields.get("dueDate") == null) {
                System.err.println("Missing required fields in line: " + line);
                return null;
            }

            return new Bill(
                    billFields.get("type"),
                    billFields.get("paymentCode"),
                    billFields.get("billNumber"),
                    userManager.findCustomerByVAT(billFields.get("issuer")),
                    userManager.findCustomerByVAT(billFields.get("customer")),
                    Double.parseDouble(billFields.get("amount")),
                    billFields.get("issueDate"),
                    billFields.get("dueDate")
            );
        } catch (Exception e) {
            System.err.println("Error parsing bill line: " + line);
            System.err.println("Error details: " + e.getMessage());
            return null;
        }
    }

    private boolean isLineInFile(String line, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return false;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String currentLine;
            while ((currentLine = reader.readLine()) != null) {
                if (currentLine.trim().equals(line.trim())) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("Error checking file: " + e.getMessage());
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
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }

    private void writeFile(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error writing file: " + e.getMessage());
        }
    }

    protected List<Bill> getBillsForCustomer(String vat) {
        List<Bill> result = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(issuedFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Bill bill = parseBill(line);
                    if (bill != null && bill.getCustomer().getVAT().equals(vat)) {
                        result.add(bill);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading issued bills: " + e.getMessage());
        }

        return result;
    }

    public void showBills(String vat, String filePath) {
        File issuedFile = new File(filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(issuedFile))) {
            if (!issuedFile.exists() || issuedFile.length() == 0) {
                System.out.println("No issued bills found for company: " + userManager.findCustomerByVAT(vat).getLegalName());
                return;
            }
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty()) continue;
                Bill bill = parseBill(line);
                if (bill != null && bill.getIssuer().getVAT().equals(vat)) {
                    String billLine = bill.marshal();
                    if (i == 0) {
                        if(filePath.equals(issuedFilePath)) {
                            System.out.println("\nIssued Bills:");
                            System.out.println("======================================================");
                        }
                        else{
                            System.out.println("\nPaid Bills:");
                            System.out.println("======================================================");
                        }
                    }
                    i++;
                    System.out.println(billLine);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading bills: " + e.getMessage());
        }
        System.out.println("======================================================");
    }


    public void manualLoadBillsFromFile(Scanner sc, String vat) {
        System.out.println("\nLoad bills from file");
        System.out.println("===================================");
        System.out.print("Enter the full path of the file to load bills from: ");
        String filePath = sc.next();
        sc.nextLine();
        File billFile = new File(filePath);
        if (!billFile.exists()) {
            System.out.println("File does not exist: " + filePath);
            return;
        }

        List<String> billsToAdd = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(billFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Bill bill = parseBill(line);
                if (bill != null && bill.getIssuer().getVAT().equals(vat)) {
                    String billLine = bill.marshal();
                    if (!isLineInFile(billLine, issuedFilePath) &&
                            !isLineInFile(billLine, expiredFilePath)) {
                        billsToAdd.add(billLine);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        if (billsToAdd.isEmpty()) {
            System.out.println("No new bills found for your company in this file.");
        } else {
            appendToFile(issuedFilePath, billsToAdd);
            System.out.println("Added " + billsToAdd.size() + " bills to issued file:");
            for (String billLine : billsToAdd) {
                System.out.println("- " + billLine);
            }
        }

        System.out.println("Press any key to continue...");
        sc.nextLine();
    }

    public String getIssuedFilePath(){
        return issuedFilePath;
    }

    public String getPaidFilePath(){
        return paidFilePath;
    }

    public void loadBillsFromIssuedToPaidFile(String rf) {
        List<String> nonPaidBills = new ArrayList<>();
        List<String> paidBills = new ArrayList<>();

        File issuedFile = new File(issuedFilePath);
        if (!issuedFile.exists()) {
            try {
                issuedFile.createNewFile();
                return;
            } catch (IOException e) {
                System.err.println("Error creating issued bills file: " + e.getMessage());
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(issuedFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                try {
                    Bill bill = parseBill(line);
                    if (bill != null) {

                        if (rf.equals(bill.getPaymentCode())) {
                            if (!isLineInFile(line, paidFilePath)) {
                                paidBills.add(line);
                            }
                        } else {
                            nonPaidBills.add(line);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing bill: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading issued bills: " + e.getMessage());
            return;
        }

        if (!paidBills.isEmpty()) {
            appendToFile(paidFilePath, paidBills);
            writeFile(issuedFilePath, nonPaidBills);
        }

    }
}