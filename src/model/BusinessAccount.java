package model;

public class BusinessAccount extends Account {
    double fee;

    public BusinessAccount(String iban, Customer primaryOwner, String dateCreated, double rate, double balance,double fee) {
        super(iban, primaryOwner, dateCreated, rate, balance);
        this.fee=fee;
    }

    @Override
    public String marshal() {
        return String.format("type:BusinessAccount,iban:%s,primaryOwner:%s,dateCreated:%s,rate:%.2f,balance:%.2f,fee:%.2f",
                iban, primaryOwner, dateCreated, rate, balance,fee);
    }

    @Override
    public void unmarshal(String data) {
        // keni giati ginetai sto accmanager
    }


}
