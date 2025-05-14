package model;

public class Bill {
    private String type;
    private String paymentCode;
    private String billNumber;
    private String issuer;
    private String customer;
    private double amount;
    private String issueDate;
    private String dueDate;

    public Bill(String type, String paymentCode, String billNumber,
                String issuer, String customer, double amount,
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
    public String getIssuer() { return issuer; }
    public String getCustomer() { return customer; }
    public double getAmount() { return amount; }
    public String getIssueDate() { return issueDate; }
    public String getDueDate() { return dueDate; }

    // Setters
    public void setType(String type) { this.type = type; }
    public void setPaymentCode(String paymentCode) { this.paymentCode = paymentCode; }
    public void setBillNumber(String billNumber) { this.billNumber = billNumber; }
    public void setIssuer(String issuer) { this.issuer = issuer; }
    public void setCustomer(String customer) { this.customer = customer; }
    public void setAmount(double amount) { this.amount = amount; }
    public void setIssueDate(String issueDate) { this.issueDate = issueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }
}
