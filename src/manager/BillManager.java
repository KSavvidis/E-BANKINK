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
    private final String expiredFilePath = billsFolderPath + "expired.csv";

    public BillManager() {
        loadBillsForToday();
    }

    public void loadBillsForToday() {
        loadBillsForDate(LocalDate.now());
    }

    public void loadBillsForDate(LocalDate date) {
        String filename = date.toString() + ".csv";

        File billFile = new File(billsFolderPath + filename);
        List<String> billIssued = new ArrayList<>();
        List<String> billExpired = new ArrayList<>();

        if (!billFile.exists()) {
            System.out.println("No bills found for today.");
            return;
        }

        Storable billLoader = new Storable() {
            @Override
            public String marshal() {
                return null;
            }

            @Override
            public void unmarshal(String line) {
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
                    LocalDate dueDate = LocalDate.parse(bill.getDueDate());
                    if (dueDate.isBefore(LocalDate.now())) {
                        billExpired.add(line);  // ΜΟΝΟ expired
                    } else {
                        billIssued.add(line);   // ΜΟΝΟ μη-expired
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        };

        storageManager.load(billLoader, billFile.getPath());
        loadToBillCsv(issuedFilePath, billIssued);
        loadToBillCsv(expiredFilePath, billExpired);
    }

    protected void loadToBillCsv(String filePath, List<String> lines) {
        File file = new File(filePath);

        Set<String> existingLines = new HashSet<>();
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    existingLines.add(line);
                }
            } catch (IOException e) {
                System.out.println("Error reading existing file: " + e.getMessage());
            }
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            for (String line : lines) {
                if(!existingLines.contains(line)) {
                    writer.write(line);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    protected List<Bill> getBillsForCustomer(String vat) {
        List<Bill> result = new ArrayList<>();
        for (Bill bill : bills) {
            if (bill.getCustomer().equals(vat)) {
                result.add(bill);
            }
        }
        return result;
    }

    protected void simulateForExpiry() {
        List<Bill> expired = new ArrayList<>();
        List<String> expiredLines = new ArrayList<>();

        for (Bill bill : bills) {
            LocalDate dueDate = LocalDate.parse(bill.getDueDate());
            if (dueDate.isBefore(LocalDate.now())) {
                expired.add(bill);
                expiredLines.add(bill.marshal());
            }
        }
        if (expired.isEmpty()) {
            return;
        }
        bills.removeAll(expired);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(expiredFilePath, true))) {
            for (String line : expiredLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public void loadIssuedBills(String vat) {
        List<Bill> issuedBills = new ArrayList<>();

        Storable loader = new Storable() {
            @Override
            public String marshal() {
                return null;
            }

            @Override
            public void unmarshal(String line) {
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

                    if (bill.getIssuer().equals(vat)) {
                        issuedBills.add(bill);
                    }

                } catch (Exception e) {
                    System.out.println("Error loading bill: " + e.getMessage());
                }
            }
        };

        storageManager.load(loader, issuedFilePath);

        if (issuedBills.isEmpty()) {
            System.out.println("No issued bills found for your company.");
        } else {
            System.out.println("\nIssued Bills:");
            System.out.println("======================================================");
            for (Bill bill : issuedBills) {
                System.out.printf(
                        "Type: %-10s | Code: %-10s | Amount: %8.2f | Customer: %-10s | Date: %s | Due: %s\n",
                        bill.getType(), bill.getPaymentCode(), bill.getAmount(),
                        bill.getCustomer(), bill.getIssueDate(), bill.getDueDate()
                );
            }
            System.out.println("======================================================");
        }

        System.out.println("Press Enter to continue...");
        new Scanner(System.in).nextLine();
    }
}
