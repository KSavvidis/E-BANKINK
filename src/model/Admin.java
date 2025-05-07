package model;

public class Admin extends User {
    String VAT;
    public Admin(String userName, String password,String legalName,String type,String VAT) {
        super(userName, password,legalName,type);
        this.VAT=VAT;
    }
}
