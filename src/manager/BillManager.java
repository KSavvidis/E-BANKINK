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
        simulateForExpiry(LocalDate.now());
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
                    if (dueDate.isBefore(date)) {
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

                    if (bill.getCustomer().equals(vat)) {
                        result.add(bill);
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing bill: " + e.getMessage());
                }
            }
        };

        storageManager.load(loader, issuedFilePath);
        return result;
    }


    protected void simulateForExpiry(LocalDate currentDate) {
       //evala alli mia lista me olous tous logariasmous tou issued
        List<Bill> allBills = new ArrayList<>();
        List<String> expiredBillsLines = new ArrayList<>();
        List<String> activeBillsLines = new ArrayList<>();

        //diavazei to issued
        try (BufferedReader reader = new BufferedReader(new FileReader(issuedFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Map<String, String> billFields = parseBillLine(line);
                if (billFields != null) {
                    Bill bill = createBillFromFields(billFields);
                    if (bill != null) {
                        allBills.add(bill);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading issued bills: " + e.getMessage());
            return;
        }

        //xwrizei tin lista me olous tous logariasmous se issued kai expired listesb
        for (Bill bill : allBills) {
            LocalDate dueDate = LocalDate.parse(bill.getDueDate());
            if (dueDate.isBefore(currentDate)) {
                expiredBillsLines.add(bill.marshal());
            } else {
                activeBillsLines.add(bill.marshal());
            }
        }

        //grafei tin expired lista
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(expiredFilePath, true))) {
            for (String line : expiredBillsLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error writing to expired bills: " + e.getMessage());
        }

        //grafei tin nea issued lista
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(issuedFilePath))) {
            for (String line : activeBillsLines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error updating issued bills: " + e.getMessage());
        }

        System.out.println("Updated " + expiredBillsLines.size() + " expired bills.");
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

    private Map<String, String> parseBillLine(String line) {
        Map<String, String> fields = new HashMap<>();
        String[] parts = line.split(",");

        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                fields.put(kv[0].trim(), kv[1].trim());
            }
        }
        return fields;
    }

    private Bill createBillFromFields(Map<String, String> fields) {
        try {
            return new Bill(
                    fields.get("type"),
                    fields.get("paymentCode"),
                    fields.get("billNumber"),
                    fields.get("issuer"),
                    fields.get("customer"),
                    Double.parseDouble(fields.get("amount")),
                    fields.get("issueDate"),
                    fields.get("dueDate")
            );
        } catch (Exception e) {
            System.out.println("Error creating bill: " + e.getMessage());
            return null;
        }
    }
}
