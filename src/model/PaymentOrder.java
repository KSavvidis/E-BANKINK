package model;

import manager.AccountManager;
import manager.BillManager;
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

    public String getPaymentCode() {
        return paymentCode;
    }

    public boolean isDue(LocalDate currentDate) {
        if(currentDate.isEqual(getStartDate()) || currentDate.isAfter(getStartDate()))
            if(currentDate.isBefore(getEndDate()) || currentDate.isEqual(getEndDate()))
                return true;
        return false;
    }
    @Override
    public boolean execute() {
        BillManager billManager = new BillManager();
        List<Bill> bills = billManager.findForRF(paymentCode);

        for (Bill bill : bills) {

        }
        return false;
    }

    @Override
    public LocalDate getNextExecutionDate(LocalDate currentDate) {
        return null;
    }

}
