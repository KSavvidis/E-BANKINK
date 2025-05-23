package manager;

import model.*;
import storage.FileStorageManager;
import storage.Storable;
import java.time.LocalDate;
import java.util.*;


public class StandingOrderManager {
    private final String standingOrdersFilePath = "data/orders/active.csv";
    private FileStorageManager storageManager;
    private List<StandingOrder> standingOrders = new ArrayList<>();
    private List<FailedOrder> failedOrders = new ArrayList<>();
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
                            , bill
                            , standingOrder.getFee()
                            , standingOrder.getDescription())){
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
                        , bill
                        , paymentOrder.getFee()
                        , paymentOrder.getDescription())){
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
                if (!transactionManager.performOrderTransfers(transferOrder.getChargeAccount()
                        , transferOrder.getCreditAccount()
                        , transferOrder.getAmount() + transferOrder.getFee()
                        , transferOrder.getDescription())) {
                    failedOrder.increaseCurrentTry();
                    failedOrder.setLastAttemptDate(currentDate);
                } else {
                    failedOrders.remove(failedOrder);
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
                       if(!transactionManager.performOrderTransfers(standingOrder.getChargeAccount()
                                                                , transferOrder.getCreditAccount()
                                                                , transferOrder.getAmount() + transferOrder.getFee()
                                                                , standingOrder.getDescription())){
                           FailedOrder failed = new FailedOrder(standingOrder);
                           failed.increaseCurrentTry();
                           failedOrders.add(failed);
                           failed.setLastAttemptDate(currentDate);
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
}
