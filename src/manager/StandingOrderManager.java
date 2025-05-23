package manager;

import model.*;
import storage.FileStorageManager;
import storage.Storable;

import java.io.File;
import java.time.LocalDate;
import java.util.*;


public class StandingOrderManager {
    private final String standingOrdersFilePath = "data/orders/active.csv";
    private final String expiredStandingOrdersFilePath = "data/orders/expired.csv";
    private FileStorageManager storageManager;
    private List<StandingOrder> standingOrders = new ArrayList<>();
    private List<FailedOrder> failedOrders = new ArrayList<>();
    public StandingOrderManager() {
        storageManager = new FileStorageManager();
        loadStandingOrders();
        createFileIfNotExists(expiredStandingOrdersFilePath);
    }

    public void createFileIfNotExists(String filePath) {
        File file = new File(filePath);

        if(!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
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
                    String[] keyValuePair = part.split(":", 2);
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
                                    map.get("orderId"),
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
                                    Integer.parseInt(map.get("dayOfMonth"))
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

        storageManager.load(loader, standingOrdersFilePath);
    }

    public void findBillsForPay(LocalDate currentDate) {
        AccountManager accountManager = new AccountManager();
        BillManager billManager = new BillManager();
        TransactionManager transactionManager = new TransactionManager(accountManager, billManager);

        Set<String> failedOrderIds = new HashSet<>();
        for (FailedOrder failedOrder : failedOrders) {
            if(failedOrder.getOrder() instanceof PaymentOrder){
                StandingOrder order = failedOrder.getOrder();
                failedOrderIds.add(order.getOrderID());
            }
        }
        List<StandingOrder> successfulOrders = new ArrayList<>();
        for (StandingOrder order : standingOrders) {
            if (!failedOrderIds.contains(order.getOrderID())) {
                successfulOrders.add(order);
            }
        }
        standingOrders = successfulOrders;

        for(StandingOrder standingOrder : standingOrders){
            if(standingOrder instanceof PaymentOrder){
                List<Bill> duePayments = billManager.findForRF(((PaymentOrder) standingOrder).getPaymentCode());
                for(Bill bill : duePayments){
                    if(((PaymentOrder) standingOrder).getMaxAmount()<bill.getAmount())
                        continue;
                    if(!transactionManager.performOrderPayment(standingOrder.getChargeAccount()
                            , bill, currentDate)){
                        FailedOrder failed = new FailedOrder(standingOrder);
                        failed.increaseCurrentTry();
                        failedOrders.add(failed);
                        failed.setLastAttemptDate(currentDate);
                    }
                }
            }
        }
    }

    public void failedForPayment(LocalDate currentDate) {
        TransactionManager transactionManager = new TransactionManager();
        BillManager billManager = new BillManager();
        List<Bill> duePayments;
        for(FailedOrder failedOrder : failedOrders){
            if(failedOrder.getOrder() instanceof TransferOrder){
                continue;
            }
            PaymentOrder paymentOrder =(PaymentOrder)failedOrder.getOrder();
            duePayments = billManager.findForRF(paymentOrder.getPaymentCode());
            for(Bill bill : duePayments){
                if(failedOrder.OverFailedAttempts())
                    continue;
                if(failedOrder.getLastAttemptDate().equals(currentDate))
                    continue;
                if(!transactionManager.performOrderPayment(paymentOrder.getChargeAccount()
                        , bill, currentDate)){
                    failedOrder.increaseCurrentTry();
                    failedOrder.setLastAttemptDate(currentDate);
                }
                else{
                    failedOrders.remove(failedOrder);
                }
            }
        }
    }

    public void failedForTransfer(LocalDate currentDate) {
        TransactionManager transactionManager = new TransactionManager();
        for(FailedOrder failedOrder : failedOrders){
            if(failedOrder.getOrder() instanceof PaymentOrder)
                continue;
            TransferOrder transferOrder = (TransferOrder) failedOrder.getOrder();
            if(failedOrder.OverFailedAttempts())
                continue;

            if(!failedOrder.getLastAttemptDate().equals(currentDate)) {
                if(transferOrder.getChargeAccount().getBalance() >= transferOrder.getAmount() + transferOrder.getFee()) {
                    if (!transactionManager.performOrderTransfers(transferOrder.getChargeAccount()
                            , transferOrder.getCreditAccount()
                            , transferOrder.getAmount()
                            , transferOrder.getDescription()
                            , currentDate)) {
                        failedOrder.increaseCurrentTry();
                        failedOrder.setLastAttemptDate(currentDate);
                    } else {
                        failedOrders.remove(failedOrder);
                    }
                }
                else{
                    failedOrder.increaseCurrentTry();
                    failedOrder.setLastAttemptDate(currentDate);
                }
            }
        }
    }

    public void transferTheOrders(LocalDate currentDate){

        AccountManager accountManager = new AccountManager();
        BillManager billManager = new BillManager();
        TransactionManager transactionManager = new TransactionManager(accountManager, billManager);
        for(StandingOrder standingOrder : standingOrders){
            if(standingOrder instanceof TransferOrder){
                TransferOrder transferOrder =(TransferOrder) standingOrder;
                if(currentDate.getDayOfMonth() == ((TransferOrder) standingOrder).getDayOfMonth()){
                    if(((currentDate.getMonthValue() - (standingOrder).getStartDate().getMonthValue()) %
                            ((TransferOrder) standingOrder).getFrequencyInMonths()) == 0){
                        FailedOrder failed = new FailedOrder(standingOrder);
                        if(transferOrder.getChargeAccount().getBalance() >= transferOrder.getAmount() + transferOrder.getFee()) {
                            if (!transactionManager.performOrderTransfers(standingOrder.getChargeAccount()
                                    , transferOrder.getCreditAccount()
                                    , transferOrder.getAmount()
                                    , standingOrder.getDescription()
                                    , currentDate)) {
                                failed.increaseCurrentTry();
                                failedOrders.add(failed);
                                failed.setLastAttemptDate(currentDate);
                            }
                        }
                        else{
                            failed.setLastAttemptDate(currentDate);
                            failed.increaseCurrentTry();
                        }
                    }
                }
            }
        }
    }

    public void resetCounter(LocalDate currentDate){
        for(FailedOrder failedOrder : failedOrders){
            if(failedOrder.getOrder() instanceof TransferOrder) {
                if (currentDate.isEqual(failedOrder.getLastAttemptDate().plusMonths(((TransferOrder) failedOrder.getOrder()).getFrequencyInMonths()))) {
                    failedOrder.resetCurrentTry();
                }
            }
        }
    }
    public void listStandingOrders(Scanner sc) {
        if (standingOrders.isEmpty()) {
            System.out.println("No standing orders found.");
            return;
        }

        System.out.println("Standing orders: ");
        int i = 1;
        for(StandingOrder order : standingOrders) {
            System.out.printf("%d. Type: %s\t OrderId: %s\t Charge Account:%s\t VAT of customer:%s\t Title:%s\t StartDate:%s\t DueDate:%s\n", i
                    ,order.getType()
                    ,order.getOrderID()
                    ,order.getChargeAccount().getIban()
                    ,order.getChargeAccount().getPrimaryOwner().getVAT()
                    ,order.getTitle()
                    ,order.getStartDate()
                    ,order.getEndDate());

            i++;
        }
        System.out.println("Press any key to continue...");
        sc.nextLine();
        sc.nextLine();
    }

    public void moveExpiredOrders(LocalDate currentDate) {
        // Διαβάζουμε όλες τις γραμμές από το active.csv
        List<String> activeLines = new ArrayList<>();
        storageManager.load(new Storable() {
            @Override
            public String marshal() { return null; }

            @Override
            public void unmarshal(String data) {
                activeLines.add(data);
            }
        }, standingOrdersFilePath);

        List<String> expiredLines = new ArrayList<>();
        List<String> newActiveLines = new ArrayList<>();

        for (String line : activeLines) {
            Map<String, String> map = new HashMap<>();
            String[] parts = line.split(",");
            for (String part : parts) {
                String[] keyValuePair = part.split(":", 2);
                if (keyValuePair.length == 2) {
                    map.put(keyValuePair[0].trim(), keyValuePair[1].trim());
                }
            }
            String endDateStr = map.get("endDate");
            if (endDateStr != null) {
                LocalDate endDate = LocalDate.parse(endDateStr);
                if (currentDate.isAfter(endDate)) {
                    expiredLines.add(line);
                } else {
                    newActiveLines.add(line);
                }
            } else {
                newActiveLines.add(line);
            }
        }

        // Γράφουμε τις μη ληγμένες γραμμές ξανά στο active.csv
        writeFile(standingOrdersFilePath, newActiveLines);

        // Προσθέτουμε τις ληγμένες στο expired.csv
        String expiredFilePath = "data/orders/expired.csv";
        appendToFile(expiredFilePath, expiredLines);

        // Αφαιρούμε από τη λίστα standingOrders τα ληγμένα χωρίς lambdas
        Iterator<StandingOrder> iterator = standingOrders.iterator();
        while (iterator.hasNext()) {
            StandingOrder order = iterator.next();
            if (currentDate.isAfter(order.getEndDate())) {
                iterator.remove();
            }
        }
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
    }   private void writeFile(String filePath, List<String> lines) {
        try {
            new java.io.PrintWriter(filePath).close(); // Clear file
        } catch (Exception e) {
            System.err.println("Error clearing file: " + e.getMessage());
        }

        appendToFile(filePath, lines);
    }
}
