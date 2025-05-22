package manager;

import model.*;
import storage.FileStorageManager;
import storage.Storable;

import java.io.File;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class StandingOrderManager {
    private String standingOrdersFilePath = "data/orders/active.csv";
    private FileStorageManager storageManager;
    private List<StandingOrder> standingOrders = new ArrayList<>();

    private String issuedFilePath = "data/bills/issued.csv";
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

    public void findBillsForPay(){
        AccountManager accountManager = new AccountManager();
        BillManager billManager = new BillManager();
        TransactionManager transactionManager = new TransactionManager(accountManager, billManager);
        List<Bill> duePayments = new ArrayList<>();

        Set<String> failedOrderIds = failedOrders
                .stream()
                .map(failedOrder ->((PaymentOrder) failedOrder.getOrder()).getOrderID())
                .collect(Collectors.toSet());

        standingOrders = standingOrders
                .stream()
                .filter(order -> !failedOrderIds.contains(order.getOrderID()))
                .collect(Collectors.toList());

        for(StandingOrder standingOrder : standingOrders){
            if(standingOrder instanceof PaymentOrder){
                duePayments = billManager.findForRF(((PaymentOrder) standingOrder).getPaymentCode());
                for(Bill bill : duePayments){
                    if(!transactionManager.performOrderPayment(standingOrder.getChargeAccount(), bill)){
                        FailedOrder failed = new FailedOrder((PaymentOrder)standingOrder);
                        failed.increaseCurrentTry();
                        failedOrders.add(failed);
                    }
                }
            }
        }
    }

    public void failedForPayment(){
        TransactionManager transactionManager = new TransactionManager();
        BillManager billManager = new BillManager();
        List<Bill> duePayments = new ArrayList<>();
        for(FailedOrder failedOrder : failedOrders){
            PaymentOrder paymentOrder =(PaymentOrder)failedOrder.getOrder();
            duePayments = billManager.findForRF(paymentOrder.getPaymentCode());
            for(Bill bill : duePayments){
                if(failedOrder.OverFailedAttempts())
                    continue;
                if(failedOrder.getCurrentTry() == 1){
                    failedOrder.increaseCurrentTry();
                    continue;
                }
                if(!transactionManager.performOrderPayment(paymentOrder.getChargeAccount(), bill)){
                    failedOrder.increaseCurrentTry();
                }
                else{
                    failedOrders.remove(failedOrder);
                }
            }

        }
    }

    public List<StandingOrder> ListStandingOrders(){
        return standingOrders;
    }

    public void failedForTransfer(){
        TransactionManager transactionManager = new TransactionManager();
        BillManager billManager = new BillManager();
        for(FailedOrder failedOrder : failedOrders){

        }
    }

    public void transferTheOrders(LocalDate currentDate){

        AccountManager accountManager = new AccountManager();
        BillManager billManager = new BillManager();
        TransactionManager transactionManager = new TransactionManager(accountManager, billManager);
        for(StandingOrder standingOrder : standingOrders){
            if(standingOrder instanceof TransferOrder){
                if(currentDate.getDayOfMonth() == ((TransferOrder) standingOrder).getDayOfMonth()){
                    if(((currentDate.getMonthValue() - ((TransferOrder)standingOrder).getStartDate().getMonthValue()) %
                            ((TransferOrder) standingOrder).getFrequencyInMonths()) == 0){
                       if(!transactionManager.performOrderTransfers((standingOrder).getChargeAccount()
                                                                , ((TransferOrder)standingOrder).getCreditAccount()
                                                                , ((TransferOrder)standingOrder).getAmount()
                                                                , (standingOrder).getDescription())){
                           FailedOrder failed = new FailedOrder((TransferOrder)standingOrder);
                           failed.increaseCurrentTry();
                           System.out.println(failed.getCurrentTry());
                           failedOrders.add(failed);
                        }
                    }
                }
            }
        }
    }

    public void resetCounter(LocalDate currentDate){
        for(FailedOrder failedOrder : failedOrders){
            failedOrder.resetCurrentTry();
        }

    }

}
