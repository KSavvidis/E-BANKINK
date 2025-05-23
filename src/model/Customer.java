package model;

public abstract class Customer extends User {
    String VAT;
    public Customer(String userName, String password,String legalName,String type, String VAT) {
        super(userName, password, legalName, type);
        this.VAT = VAT;
    }

    public String getVAT() {
        return VAT;
    }

}
