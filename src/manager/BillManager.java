package manager;

import model.Bill;
import storage.FileStorageManager;
import storage.Storable;

import java.util.*;

public class BillManager {
    private final List<Bill> bills = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String billsFilePath = "data/bills/2025-05-01.csv";

    public BillManager() {
        loadBills();
    }

    public void loadBills() {
        Storable loader = new Storable() {
            @Override
            public String marshal() {
                return null;
            }

            @Override
            public void unmarshal(String data) {
                Map<String, String> map = new HashMap<>();
                String[] parts = data.split(",");

                for (String part : parts) {
                    String[] kv = part.split(":", 2);
                    if (kv.length == 2) {
                        map.put(kv[0].trim(), kv[1].trim());
                    }
                }

                Bill bill = null;
                try {
                    bill = new Bill(
                            map.get("type"),
                            map.get("paymentCode"),
                            map.get("billNumber"),
                            map.get("issuer"),
                            map.get("customer"),
                            Double.parseDouble(map.get("amount")),
                            map.get("issueDate"),
                            map.get("dueDate")
                    );
                    bills.add(bill);
                } catch (Exception e) {
                    System.out.println("Error parsing bill: " + e.getMessage());
                }
            }
        };

        storageManager.load(loader, billsFilePath);
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
