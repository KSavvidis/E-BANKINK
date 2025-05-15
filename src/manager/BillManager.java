package manager;

import model.Bill;
import storage.FileStorageManager;
import storage.Storable;

import java.util.*;
import java.io.File;
import java.io.FilenameFilter;

public class BillManager {
    private final List<Bill> bills = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String billsFilePath = "data/bills/2025-05-01.csv";

    public BillManager() {
        loadBills();
    }

    public void loadBills() {
        // O fakelos pou periexei ta arxeia twn logariasmwn
        File billsDirectory = new File("data/bills");

        // Elegxos an yparxei o fakelos kai an einai pragmatika fakelos
        if (!billsDirectory.exists() || !billsDirectory.isDirectory()) {
            System.out.println("Den vrethike o fakelos logariasmwn h den einai fakelos.");
            return;
        }

        // Filtraroume ola ta .csv arxeia xwris xrhsh lambdas
        FilenameFilter csvFilter = new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".csv");
            }
        };

        File[] billFiles = billsDirectory.listFiles(csvFilter);

        if (billFiles == null || billFiles.length == 0) {
            System.out.println("Den vrethikan CSV arxeia ston fakelo logariasmwn.");
            return;
        }

        // Gia kathe arxeio logariasmou fortwnoume ta dedomena tou
        for (File billFile : billFiles) {

            Storable billLoader = new Storable() {
                @Override
                public String marshal() {
                    return null;
                }

                @Override
                public void unmarshal(String line) {
                    // Analysi grammis se key-value pairs
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
                        System.out.println("Lathos kata to parsing tou logariasmou sto arxeio '" + billFile.getName() + "': " + e.getMessage());
                    }
                }
            };

            // Fortwsi arxeiou me ton storage manager
            storageManager.load(billLoader, billFile.getPath());
        }
    }

    // Epistrefei lista logariasmwn gia sigkekrimeno pelati (VAT)
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
