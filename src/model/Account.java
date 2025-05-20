package model;

import storage.Storable;

import java.util.List;

public abstract class Account implements Storable {
    protected String iban;
    protected Customer primaryOwner;
    protected List<Customer> coOwner; //List<User>
    protected String dateCreated; // mporei LocalDate
    protected double rate;
    protected double balance;

    public Account(String iban, Customer primaryOwner, List<Customer> coOwner, String dateCreated, double rate, double balance) {
        this.iban = iban;
        this.primaryOwner = primaryOwner;
        this.coOwner = coOwner;
        this.dateCreated = dateCreated;
        this.rate = rate;
        this.balance = balance;
    }

    public Account(String iban, Customer primaryOwner, String dateCreated, double rate, double balance) {
        this.iban = iban;
        this.primaryOwner = primaryOwner;
        this.dateCreated = dateCreated;
        this.rate = rate;
        this.balance = balance;
    }

    public Account(String iban, double balance){
        this.iban = iban;
        this.balance = balance;
    }
    //2os constructor gia to businessAccount
    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public Customer getPrimaryOwner() {
        return primaryOwner;
    }

    public void setPrimaryOwner(Customer primaryOwner) {
        this.primaryOwner = primaryOwner;
    }

    public List<Customer> getCoOwner() {
        return coOwner;
    }

    public void setCoOwner(List<Customer> coOwner) {
        this.coOwner = coOwner;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public double getRate() {
        return rate;
    }

    public void setRate(double rate) {
        this.rate = rate;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public String marshal(){
        return null;
    }
    @Override
    public void unmarshal(String data) {

        // menei keni giati ulopoieitai ston AccountManager
    }
}