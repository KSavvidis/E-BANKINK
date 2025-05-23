package model;

import java.time.LocalDate;

public class TransferOrder extends StandingOrder {
    private double amount;
    private Account creditAccount;
    private int frequencyInMonths;
    private int dayOfMonth;

    public TransferOrder(String type, String orderID, String title, String description, Customer customer, LocalDate startDate, LocalDate endDate, double fee, Account chargeAccount, double amount, Account creditAccount, int frequencyInMonths, int dayOfMonth) {
        super(type, orderID, title, description, customer, startDate, endDate, fee, chargeAccount);
        this.amount = amount;
        this.creditAccount = creditAccount;
        this.frequencyInMonths = frequencyInMonths;
        this.dayOfMonth = dayOfMonth;
    }

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public double getAmount() {
        return amount;
    }

    public Account getCreditAccount() {
        return creditAccount;
    }
    public int getFrequencyInMonths(){
        return frequencyInMonths;
    }

}
