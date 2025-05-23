package model;

import java.time.LocalDate;

public abstract class StandingOrder{
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

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;

    }
    public LocalDate getStartDate() {
        return startDate;
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

    public String getTitle() {
        return title;
    }

    public Account getChargeAccount() {
        return chargeAccount;
    }


}
