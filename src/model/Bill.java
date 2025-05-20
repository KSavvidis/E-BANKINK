package model;

import manager.UserManager;
import storage.Storable;

import java.util.HashMap;
import java.util.Map;

public class Bill implements Storable {
    private String type;
    private String paymentCode;
    private String billNumber;
    private Customer issuer;// prepei na ginei user
    private Customer customer; // epishs user
    private double amount;
    private String issueDate; //mporei LocalDate
    private String dueDate; // mporei LocalDate

    public Bill(String type, String paymentCode, String billNumber,
                Customer issuer, Customer customer, double amount,
                String issueDate, String dueDate) {
        this.type = type;
        this.paymentCode = paymentCode;
        this.billNumber = billNumber;
        this.issuer = issuer;
        this.customer = customer;
        this.amount = amount;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
    }

    // Getters
    public String getType() { return type; }
    public String getPaymentCode() { return paymentCode; }
    public String getBillNumber() { return billNumber; }
    public Customer getIssuer() { return issuer; }
    public Customer getCustomer() { return customer; }
    public double getAmount() { return amount; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }

    // Setters
    public void setType(String type) { this.type = type; }
    public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    public void setIssuer(Customer issuer) { this.issuer = issuer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }


    @Override
    public String marshal() {
        StringBuilder sb = new StringBuilder();
        sb.append("type:").append(type)
        .append(",paymentCode:").append(paymentCode)
        .append(",billNumber:").append(billNumber)
        .append(",issuer:").append(issuer)
        .append(",customer:").append(customer)
        .append(",amount:").append(String.format("%.2f", amount))
        .append(",issueDate:").append(issueDate)
        .append(",dueDate:").append(dueDate);
        return sb.toString();
    }

    @Override
    public void unmarshal(String data) {
        UserManager userManager = new UserManager();
        Map<String, String> fields = new HashMap<>();
        String[] pairs = data.split(",");

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            if (keyValue.length == 2) {
                fields.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        this.type = fields.get("type");
        this.paymentCode = fields.get("paymentCode");
        this.billNumber = fields.get("billNumber");
        this.issuer = userManager.findCustomerByVAT(fields.get("issuer"));
        this.customer = userManager.findCustomerByVAT(fields.get("customer"));
        this.amount = Double.parseDouble(fields.get("amount"));
        this.issueDate = fields.get("issueDate");
        this.dueDate = fields.get("dueDate");

    }
}
