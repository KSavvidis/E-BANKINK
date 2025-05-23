package manager;

import model.Bill;
import storage.FileStorageManager;
import storage.Storable;

import java.io.*;
import java.time.LocalDate;
import java.util.*;


public class BillManager {

    private final FileStorageManager storageManager = new FileStorageManager();
    private final UserManager userManager = new UserManager();
    private final String billsFolderPath = "data/bills/";
    private final String issuedFilePath = billsFolderPath + "issued.csv";
    private final String expiredFilePath = billsFolderPath + "expired.csv";
    private final String paidFilePath = billsFolderPath + "paid.csv";

    public BillManager() {
        createFileIfNotExists(issuedFilePath);
        createFileIfNotExists(expiredFilePath);
        createFileIfNotExists(paidFilePath);

        loadBillsForToday();
    }

    public void loadBillsForToday() {
        loadBillsForDate(LocalDate.now());
        simulateForExpiry(LocalDate.now());
    }

    private void createFileIfNotExists(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                System.err.println("Error creating file: " + filePath);
            }
        }
    }

    public void loadBillsForDate(LocalDate date) {
        String filename = date.toString() + ".csv";
        File billFile = new File(billsFolderPath + filename);

        if (!billFile.exists()) {
            return;
        }

        Storable loader = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                line = line.trim();
                if (line.isEmpty()) return;

                try {
                    Bill bill = parseBill(line);
                    if (bill != null) {
                        LocalDate dueDate = LocalDate.parse(bill.getDueDate());
                        if (dueDate.isBefore(date)) {
                            if (!isLineInFile(line, expiredFilePath) && !isLineInFile(line, paidFilePath)) {
                                appendToFile(expiredFilePath, Collections.singletonList(line));
                            }
                        } else {
                            if (!isLineInFile(line, issuedFilePath) && !isLineInFile(line, paidFilePath)) {
                                appendToFile(issuedFilePath, Collections.singletonList(line));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                }
            }
        };
        storageManager.load(loader, billsFolderPath + filename);
    }

    protected void simulateForExpiry(LocalDate currentDate) {
        List<String> activeBills = new ArrayList<>();
        List<String> expiredBills = new ArrayList<>();

        Storable processor = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                line = line.trim();
                if (line.isEmpty()) return;

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
        };

        storageManager.load(processor, issuedFilePath);

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
        final boolean[] found = {false};

        Storable checker = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String currentLine) {
                if (currentLine.trim().equals(line.trim())) {
                    found[0] = true;
                }
            }
        };

        storageManager.load(checker, filePath);
        return found[0];
    }

    private void appendToFile(String filePath, List<String> lines) {
        if (lines.isEmpty()) return;

        for (String line : lines) {
            storageManager.save(new Storable() {
                @Override
                public String marshal() { return line; }

                @Override
                public void unmarshal(String data) {}
            }, filePath, true);
        }
    }

    private void writeFile(String filePath, List<String> lines) {
        try {
            new java.io.PrintWriter(filePath).close(); // Clear file
        } catch (Exception e) {
            System.err.println("Error clearing file: " + e.getMessage());
        }

        appendToFile(filePath, lines);
    }


    protected List<Bill> getBillsForCustomer(String vat) {
        List<Bill> result = new ArrayList<>();

        Storable collector = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                line = line.trim();
                if (line.isEmpty()) return;

                try {
                    Bill bill = parseBill(line);
                    if (bill != null && bill.getCustomer().getVAT().equals(vat)) {
                        result.add(bill);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                }
            }
        };

        storageManager.load(collector, issuedFilePath);
        return result;
    }

    public List<Bill> findForRF(String rf){
        List<Bill> result = new ArrayList<>();
        Storable collector = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                line = line.trim();
                if (line.isEmpty()) return;

                try {

                    Bill bill = parseBill(line);
                    if (bill != null && bill.getPaymentCode().equals(rf)) {
                        result.add(bill);
                    }
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                }
            }
        };
        storageManager.load(collector, issuedFilePath);
        return result;
    }
    public void showBills(String vat, String filePath) {
        File issuedPaidFile = new File(filePath);

        try (BufferedReader reader = new BufferedReader(new FileReader(issuedPaidFile))) {
            if (!issuedPaidFile.exists() || issuedPaidFile.length() == 0) {
                System.out.println("No issued/paid bills found for company: " + userManager.findCustomerByVAT(vat).getLegalName());
                return;
            }
            boolean foundBills = false;
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
                        foundBills = true;
                    }
                    i++;
                    System.out.println(billLine);
                }

            }
            if(!foundBills){
                System.out.println("No issued/paid bills found for company: " + userManager.findCustomerByVAT(vat).getLegalName());
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
        String filePath = sc.nextLine().trim();  // Αλλαγή από next() σε nextLine()

        List<String> billsToAdd = new ArrayList<>();

        Storable loader = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                line = line.trim();
                if (line.isEmpty()) return;

                try {
                    Bill bill = parseBill(line);
                    if (bill != null && bill.getIssuer().getVAT().equals(vat)) {
                        String billLine = bill.marshal();
                        if (!isLineInFile(billLine, issuedFilePath) &&
                                !isLineInFile(billLine, expiredFilePath)) {
                            billsToAdd.add(billLine);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing line: " + line);
                }
            }
        };

        // Χρήση FileStorageManager για φόρτωση
        storageManager.load(loader, filePath);

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
        Storable processor = new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String line) {
                line = line.trim();
                if (line.isEmpty()) return;

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
        };

        // Φόρτωση με FileStorageManager
        storageManager.load(processor, issuedFilePath);

        if (!paidBills.isEmpty()) {
            appendToFile(paidFilePath, paidBills);
            writeFile(issuedFilePath, nonPaidBills);
        }
    }
    public boolean hasBillsForDate(LocalDate date) {
        String filename = date.toString() + ".csv";
        File billFile = new File(billsFolderPath + filename);
        return billFile.exists();
    }

}
