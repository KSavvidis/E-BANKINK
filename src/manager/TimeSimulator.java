package manager;

import model.Account;
import transaction.DepositTransaction;
import transaction.Transaction;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class TimeSimulator{
    private AccountManager accountManager;
    private BillManager billManager;
    private StandingOrderManager standingOrderManager;
    private static LocalDate currentDate;
    private Map<Account, Double> monthlyRate = new HashMap<>();
   /* public TimeSimulator(AccountManager accountManager, BillManager billManager){
        this.accountManager = new AccountManager();
        this.billManager = new BillManager();
    }*/

    public TimeSimulator(AccountManager accountManager, BillManager billManager, StandingOrderManager standingOrderManager){
        this.accountManager = accountManager;
        this.billManager = billManager;
        this.standingOrderManager = standingOrderManager;
    }

    public TimeSimulator(){}

    public void run(LocalDate endDate ){
        LocalDate currentDate = LocalDate.now();

        if(endDate.isBefore(currentDate)){
            System.out.println("Please enter a valid date that is not in the past.");
        }
        else{
            while(!currentDate.isAfter(endDate)) {
                dailyWork(currentDate);
                currentDate = currentDate.plusDays(1);
            }

        }
        accountManager.saveAccounts();
        System.out.println("Time simulated to date " + (currentDate.minusDays(1)) + ".");
        this.currentDate = currentDate.minusDays(1);
    }

    private void dailyWork(LocalDate currentDate){
        rate();
        fee();
        billManager.loadBillsForDate(currentDate);
        billManager.simulateForExpiry(currentDate);
    }
    private void rate(){
/*
        for(Account account : accountManager.getAllAccounts()){
            double dailyInterest = account.getBalance() * account.getRate()/365.0;
            if(monthlyRate.containsKey(account)){
                monthlyRate.put(account, monthlyRate.get(account) + dailyInterest);
            }
            else {
                monthlyRate.put(account, dailyInterest);
            }

            if(currentDate.getDayOfMonth() == 30){
                Transaction depositRate = new DepositTransaction(new TransactionManager());
            }
        }*/
    }

    private void fee(){

    }

    public LocalDate getCurrentDate(){
        return currentDate;
    }
}
