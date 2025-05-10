package model;

public class PersonalAccount extends Account {
    public PersonalAccount(String iban, String primaryOwner, String dateCreated, double rate, double balance) {
        super(iban, primaryOwner, dateCreated, rate, balance);
    }
    @Override
    public String marshal() {
        return String.format("type:PersonalAccount,iban:%s,primaryOwner:%s,dateCreated:%s,rate:%.2f,balance:%.2f",
                iban, primaryOwner, dateCreated, rate, balance);
    }

    @Override
    public void unmarshal(String data) {
        // keni giati ginetai sto accmanager
    }


}
