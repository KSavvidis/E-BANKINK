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

        // fortwsi apo to arxeio xristwn
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
                    if(!transactionManager.performOrderPayment(standingOrder.getChargeAccount(), bill)){
                        FailedOrder failed = new FailedOrder((PaymentOrder)standingOrder);
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
        List<Bill> duePayments = new ArrayList<>();
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
                    if(!transactionManager.performOrderPayment(paymentOrder.getChargeAccount(), bill)){
                        failedOrder.increaseCurrentTry();
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
            if(failedOrder.OverFailedAttempts()) {
                System.out.println(failedOrder.getCurrentTry());
                continue;
            }
            System.out.println(failedOrder.getLastAttemptDate().toString());
            System.out.println("current try tou transfer sto failed" + failedOrder.getCurrentTry());
            if(!failedOrder.getLastAttemptDate().equals(currentDate)) {
                if (!transactionManager.performOrderTransfers(transferOrder.getChargeAccount()
                        , transferOrder.getCreditAccount()
                        , transferOrder.getAmount()
                        , transferOrder.getDescription())) {
                    failedOrder.increaseCurrentTry();
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
                                , transferOrder.getAmount()
                                , standingOrder.getDescription())){
                            FailedOrder failed = new FailedOrder(standingOrder);
                            failed.increaseCurrentTry();
                            System.out.println(failed.getCurrentTry());
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
            if(currentDate.isEqual(failedOrder.getLastAttemptDate().plusMonths(1))){
                failedOrder.resetCurrentTry();
            }

        }

    }

}