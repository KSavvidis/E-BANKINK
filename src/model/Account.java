package model;

import storage.Storable;

import java.util.List;

public abstract class Account implements Storable {
    protected String iban;
    protected Customer primaryOwner;
    protected List<Customer> coOwner;
    protected String dateCreated;
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


    public Customer getPrimaryOwner() {
        return primaryOwner;
    }

    public List<Customer> getCoOwner() {
        return coOwner;
    }

    public String getDateCreated() {
        return dateCreated;
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
    }
}