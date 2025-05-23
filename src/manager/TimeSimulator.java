package manager;

import model.Account;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TimeSimulator{
    private AccountManager accountManager;
    private BillManager billManager;
    private StandingOrderManager standingOrderManager;
    private LocalDate currentDate;
    private Map<Account, Double> monthlyRate = new HashMap<>();

    public TimeSimulator(AccountManager accountManager, BillManager billManager, StandingOrderManager standingOrderManager){
        this.accountManager = accountManager;
        this.billManager = billManager;
        this.standingOrderManager = standingOrderManager;
        this.currentDate = LocalDate.now();
    }

    public TimeSimulator(){}

    public void run(LocalDate endDate ){
        if(endDate.isBefore(currentDate)){
            System.out.println("Please enter a valid date that is not in the past.");
        }
        else{
            while(!currentDate.isAfter(endDate)) {
                dailyWork(currentDate);
                currentDate = currentDate.plusDays(1);
            }
        }

        System.out.println("Time simulated to date " + (currentDate.minusDays(1)) + ".");
        this.currentDate = currentDate.minusDays(1);
    }

    private void dailyWork(LocalDate currentDate){
        System.out.println("Today's date: " + currentDate.toString());
        TransactionManager transactionManager = new TransactionManager(accountManager, billManager);
        this. monthlyRate = accountManager.applyRate(currentDate,monthlyRate, transactionManager);
        accountManager.applyFee(currentDate, transactionManager);
        billManager.loadBillsForDate(currentDate);
        billManager.simulateForExpiry(currentDate);
        standingOrderManager.findBillsForPay(currentDate);
        standingOrderManager.transferTheOrders(currentDate);
        standingOrderManager.failedForPayment(currentDate);
        standingOrderManager.failedForTransfer(currentDate);
        if(currentDate.getDayOfMonth() == 1){
            standingOrderManager.resetCounter(currentDate);
        }
        if (!billManager.hasBillsForDate(currentDate)) {
            System.out.println("No bills found today.");
        } else {
            billManager.loadBillsForDate(currentDate);
        }
        System.out.println("-----------------------------------------");
    }

    public LocalDate getCurrentDate(){
        return currentDate;
    }
}
