package model;


import transaction.Transaction;

import java.time.LocalDate;
import java.util.List;

public abstract class StandingOrder {
    private String type;
    private String orderID;
    private String title;
    private String description;
    private User customer;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double fee;
    private Account chargeAccount;
    private int failedAttempts;

    public StandingOrder(String type, String orderID, String title, String description, Customer customer, LocalDate startDate, LocalDate endDate, Double fee, Account chargeAccount) {
        this.type = type;
        this.orderID = orderID;
        this.title = title;
        this.description = description;
        this.customer = customer;
        this.startDate = startDate;
        this.endDate = endDate;
        this.fee = fee;
        this.chargeAccount = chargeAccount;
        this.failedAttempts = 0;
    }

    public boolean isActiveOn(LocalDate date) {
        if(startDate.isBefore(date) || endDate.isAfter(date) && startDate.isEqual(date) || endDate.isEqual(date)) {
            return true;
        }
        return false;
    }

    public void increaseFailedAttempts() {
        failedAttempts++;
    }
    public int getFailedAttempts() {
        return failedAttempts;
    }

    public boolean failedTooManyAttempts() {
        return failedAttempts >= 3;
    }
    public LocalDate getStartDate() {
        return startDate;
    }

    public void resetFailedAttempts() {
        failedAttempts = 0;
    }
    public String getDescription() {
        return description;
    }

    public String getOrderID() {
        return orderID;
    }
    public double getFee() {
        return fee;
    }
    public LocalDate getEndDate() {
        return endDate;
    }

    public Account getChargeAccount() {
        return chargeAccount;
    }
    public abstract boolean validBalance();
    public abstract boolean executeOn(LocalDate date);
    public abstract boolean execute();
    public abstract LocalDate getNextExecutionDate(LocalDate currentDate);
}
