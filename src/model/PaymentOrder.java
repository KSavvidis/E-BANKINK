package model;

import manager.AccountManager;
import manager.TimeSimulator;
import transaction.Transaction;

import java.time.LocalDate;
import java.util.List;

public class PaymentOrder extends StandingOrder {
    private double maxAmount;
    private String paymentCode;

    public PaymentOrder(String type, String orderID, String title, String description, Customer customer, LocalDate startDate, LocalDate endDate, double fee, Account chargeAccount, double maxAmount, String paymentCode) {
        super(type, orderID, title, description, customer, startDate, endDate, fee, chargeAccount);
        this.maxAmount = maxAmount;
        this.paymentCode = paymentCode;
    }
    @Override
    public boolean canBeExecuted() {
        TimeSimulator timeSimulator = new TimeSimulator(null, null , null);
        LocalDate currentDate = timeSimulator.getCurrentDate();
        boolean inPeriod = false;
        boolean validBalance = false;

        if(!currentDate.isBefore(getStartDate()) || !currentDate.isAfter(getEndDate())) {
            inPeriod =  true;
        }
        if(getChargeAccount().getBalance() >= maxAmount) {
            validBalance = true;
        }

        return inPeriod && validBalance;
    }

    @Override
    public boolean execute(List<Transaction> transactions) {
        return false;
    }

}
