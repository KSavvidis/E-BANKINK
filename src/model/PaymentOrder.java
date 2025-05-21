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
    public boolean executeOn(LocalDate date) {
        return false;
    }

    @Override
    public boolean execute() {
        return false;
    }
}
