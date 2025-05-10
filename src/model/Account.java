package model;

import storage.Storable;

public abstract class Account implements Storable {
    protected String iban;
    protected String primaryOwner;
    protected String dateCreated;
    protected double rate;
    protected double balance;

    public Account(String iban, String primaryOwner, String dateCreated, double rate, double balance) {
        this.iban = iban;
        this.primaryOwner = primaryOwner;
        this.dateCreated = dateCreated;
        this.rate = rate;
        this.balance = balance;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getPrimaryOwner() {
        return primaryOwner;
    }

    public void setPrimaryOwner(String primaryOwner) {
        this.primaryOwner = primaryOwner;
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
    public abstract String marshal();
    @Override
    public void unmarshal(String data) {

        // menei keni giati ulopoieitai ston AccountManager
    }


}