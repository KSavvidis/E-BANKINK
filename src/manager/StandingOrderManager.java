package manager;

import model.*;
import storage.FileStorageManager;
import storage.Storable;

import java.io.File;
import java.time.LocalDate;
import java.util.*;

public class StandingOrderManager {
    private String standingOrdersFilePath = "data/orders/active.csv";
    private FileStorageManager storageManager;
    private List<StandingOrder> standingOrders = new ArrayList<>();
    public StandingOrderManager() {
        storageManager = new FileStorageManager();
        loadStandingOrders();
    }

    public void loadStandingOrders() {

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
                    String[] keyValuePair = part.split(":", 2);  // xwrizoume to key kai to value
                    if (keyValuePair.length == 2) {
                        String key = keyValuePair[0].trim();
                        String value = keyValuePair[1].trim();
                        map.put(key, value);
                    }
                }
                StandingOrder standingOrder = null;
                UserManager userManager = new UserManager();
                AccountManager accountManager = new AccountManager();
                String type = map.get("type");
                try {
                    switch (type) {
                        case "TransferOrder":
                            standingOrder = new TransferOrder(
                                    map.get("type"),
                                    map.get("orderID"),
                                    map.get("title"),
                                    map.get("description"),
                                    userManager.findCustomerByVAT(map.get("customer")),
                                    LocalDate.parse(map.get("startDate")),
                                    LocalDate.parse(map.get("endDate")),
                                    Double.parseDouble(map.get("fee")),
                                    accountManager.findByIban(map.get("chargeAccount")),
                                    Double.parseDouble(map.get("amount")),
                                    accountManager.findByIban(map.get("creditAccount")),
                                    Integer.parseInt(map.get("frequencyInMonths")),
                                    Integer.parseInt(map.get("DayOfMonth"))
                            );
                            break;
                        case "PaymentOrder":
                            standingOrder = new PaymentOrder(
                                    map.get("type"),
                                    map.get("orderId"),
                                    map.get("title"),
                                    map.get("description"),
                                    userManager.findCustomerByVAT(map.get("customer")),
                                    LocalDate.parse(map.get("startDate")),
                                    LocalDate.parse(map.get("endDate")),
                                    Double.parseDouble(map.get("fee")),
                                    accountManager.findByIban(map.get("chargeAccount")),
                                    Double.parseDouble(map.get("maxAmount")),
                                    map.get("paymentCode")
                            );
                            break;
                    }
                    standingOrders.add(standingOrder);
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        };

        // fortwsi apo to arxeio xristwn
        storageManager.load(loader, "data/users/users.csv");
    }

    public void executeStandingOrders(LocalDate currentDate) {
        PriorityQueue<StandingOrder> standingOrdersQueue = new PriorityQueue<>();

        for(StandingOrder standingOrder : standingOrders) {

        }
    }

}
