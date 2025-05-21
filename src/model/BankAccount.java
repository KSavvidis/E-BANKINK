package model;

public class BankAccount extends Account {
    private static BankAccount bankAccount;
    private String owner;

    public BankAccount(String iban, double balance, String owner) {
        super(iban, balance);
        this.owner = owner;
    }

    public void matchBankAccount(BankAccount bankAccount) {
        if(this.bankAccount == null){
            this.bankAccount = bankAccount;
        }
    }
    public static BankAccount getInstance(){
        if(bankAccount == null){
            System.out.println("Bank's account is unavailable");
            return null;
        }
        return bankAccount;
    }
}
