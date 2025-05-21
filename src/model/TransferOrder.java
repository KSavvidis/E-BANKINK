package model;

import manager.TimeSimulator;
import manager.TransactionManager;
import transaction.Transaction;
import transaction.TransferOrderTransaction;
import transaction.TransferTransaction;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransferOrder extends StandingOrder {
    private double amount;
    private Account creditAccount;
    private int frequencyInMonths;
    private int dayOfMonth;

    public TransferOrder(String type, String orderID, String title, String description, Customer customer, LocalDate startDate, LocalDate endDate, Double fee, Account chargeAccount, double amount, Account creditAccount, int frequencyInMonths, int dayOfMonth) {
        super(type, orderID, title, description, customer, startDate, endDate, fee, chargeAccount);
        this.amount = amount;
        this.creditAccount = creditAccount;
        this.frequencyInMonths = frequencyInMonths;
        this.dayOfMonth = dayOfMonth;
    }

    public boolean canBeExecuted() {
        TimeSimulator timeSimulator = new TimeSimulator();
        LocalDate currentDate = timeSimulator.getCurrentDate();
        return validBalance() && isActiveOn(currentDate) && frequencyOfMonthCharged(currentDate) && !failedTooManyAttempts();
    }

    private boolean frequencyOfMonthCharged(LocalDate currentDate) {
        int monthsBetween = currentDate.getMonthValue() - getStartDate().getMonthValue();
        return monthsBetween % frequencyInMonths == 0;
    }
    @Override
    public boolean executeOn(LocalDate date) {
        if(!isActiveOn(date)) {
            return false;
        }
        return dayOfMonth == date.getDayOfMonth();
    }

    @Override
    public boolean execute() {
        TimeSimulator timeSimulator = new TimeSimulator();
        if(canBeExecuted()) {
            TransactionManager transactionManager = new TransactionManager();
            transactionManager.performOrderTransfer(getChargeAccount(), creditAccount, amount, getDescription());
            transactionManager.performOrderTransferFee(getChargeAccount(), getFee(), getDescription());
            resetFailedAttempts();
            return true;
        }
        increaseFailedAttempts();
        return false;
    }

    @Override
    public boolean validBalance() {
        if(getChargeAccount().getBalance() >= amount + (getFee()/100) * amount) {
            return true;
        }
        return false;
    }

    @Override
    public LocalDate getNextExecutionDate(LocalDate currentDate) {
        LocalDate next = getStartDate();
        while(next.isAfter(getEndDate())) {
            if(next.getDayOfMonth() == dayOfMonth && !next.isBefore(currentDate)) {
                return next;
            }

        }
        return null;
    }
}
