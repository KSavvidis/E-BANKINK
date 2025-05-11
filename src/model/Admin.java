package model;

public class Admin extends User {
    public Admin(String userName, String password,String legalName,String type) {
        super(userName, password,legalName,type);
    }
    public String getVAT(){
        return null;
    }
}
