package model;

import java.time.LocalDate;

public class PaymentOrder extends StandingOrder {
    private double maxAmount;
    private String paymentCode;

    public PaymentOrder(String type, String orderID, String title, String description, Customer customer, LocalDate startDate, LocalDate endDate, double fee, Account chargeAccount, double maxAmount, String paymentCode) {
        super(type, orderID, title, description, customer, startDate, endDate, fee, chargeAccount);
        this.maxAmount = maxAmount;
        this.paymentCode = paymentCode;
    }

    public String getPaymentCode() {
        return paymentCode;
    }

    public double getMaxAmount() {
        return maxAmount;
    }
}
