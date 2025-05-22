package manager;

import model.Account;
import model.BankAccount;
import model.BusinessAccount;
import transaction.DepositTransaction;
import transaction.Transaction;
import transaction.TransferTransaction;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TimeSimulator{
    private AccountManager accountManager;
    private BillManager billManager;
    private final StatementManager statementManager = new StatementManager();
    private StandingOrderManager standingOrderManager;
    private LocalDate currentDate;
    private Map<Account, Double> monthlyRate = new HashMap<>();
   /* public TimeSimulator(AccountManager accountManager, BillManager billManager){
        this.accountManager = new AccountManager();
        this.billManager = new BillManager();
    }*/

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
        standingOrderManager.findBillsForPay();
        standingOrderManager.transferTheOrders(currentDate);
        standingOrderManager.failedForPayment();
        if(currentDate.getDayOfMonth() == 1){
            standingOrderManager.resetCounter(currentDate);
        }
        System.out.println("-----------------------------------------");
    }

    public LocalDate getCurrentDate(){
        return currentDate;
    }
}
