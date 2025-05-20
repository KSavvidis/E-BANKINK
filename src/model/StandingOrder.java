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
    }

    abstract boolean canBeExecuted();
    abstract boolean execute(List<Transaction> transactions);

    public LocalDate getStartDate() {
        return startDate;
    }

    public String getDescription() {
        return description;
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
}
