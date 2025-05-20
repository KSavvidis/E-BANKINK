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

    @Override
    boolean canBeExecuted() {
        TimeSimulator timeSimulator = new TimeSimulator();
        boolean inPeriod = false;
        boolean validBalance = false;
        boolean dayOfCharge = false;
        boolean frequencyOfMonthCharged = false;

        if (!timeSimulator.getCurrentDate().isBefore(getStartDate()) || !timeSimulator.getCurrentDate().isAfter(getEndDate())) {
            inPeriod = true;
        }

        if (getChargeAccount().getBalance() >= amount) {
            validBalance = true;
        }

        if (timeSimulator.getCurrentDate().getDayOfMonth() == dayOfMonth) {
            dayOfCharge = true;
        }

        if (timeSimulator.getCurrentDate().getMonthValue() % frequencyInMonths == 0) {
            frequencyOfMonthCharged = true;
        }
        return frequencyOfMonthCharged && inPeriod && validBalance && dayOfCharge;
    }

    @Override
    boolean execute(List<Transaction> transactions) {
        return false;
    }
}
