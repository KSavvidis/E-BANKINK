package manager;

import model.Account;

import java.time.LocalDate;

public class TimeSimulator{
    private AccountManager accountManager;
    private BillManager billManager;

    public TimeSimulator(AccountManager accountManager, BillManager billManager){
        this.accountManager = new AccountManager();
        this.billManager = new BillManager();
    }

    public void run(LocalDate currentDate, LocalDate endDate){
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
    }

    private void dailyWork(LocalDate currentDate){
        rate();
        fee();
        billManager.loadBillsForDate(currentDate);
        billManager.simulateForExpiry();
    }
    private void rate(){
        for(Account account : accountManager.getAllAccounts()){
            double rate =account.getRate();
            double interest = account.getBalance() * rate;
            account.setBalance(account.getBalance() + interest);
        }
    }

    private void fee(){

    }
}
