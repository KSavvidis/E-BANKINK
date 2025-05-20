package model;

import manager.AccountManager;
import manager.UserManager;

import java.util.ArrayList;
import java.util.List;

public class PersonalAccount extends Account {
    public PersonalAccount(String iban, Customer primaryOwner, List<Customer> coOwner, String dateCreated, double rate, double balance) {
        super(iban, primaryOwner,coOwner, dateCreated, rate, balance);
    }
    @Override
    public String marshal() {
        StringBuilder sb = new StringBuilder();
        sb.append("type:PersonalAccount");
        sb.append(",iban:").append(iban);
        sb.append(",primaryOwner:").append(primaryOwner.getVAT());
        sb.append(",dateCreated:").append(dateCreated);
        sb.append(",rate:").append(String.format("%.2f", rate));
        sb.append(",balance:").append(String.format("%.2f", balance));

        if (coOwner != null) {
            for (Customer co : coOwner) {
                sb.append(",coOwner:").append(co.getVAT()); // Συμβατότητα με το input format
            }
        }

        return sb.toString();
    }

    @Override
    public void unmarshal(String data) {
        this.coOwner = new ArrayList<>();  // ΠΟΛΥ ΣΗΜΑΝΤΙΚΟ: αρχικοποίηση της λίστας!
        String[] parts = data.split(",");
        UserManager userManager = new UserManager();
        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length < 2) continue;

            String key = keyValue[0];
            String value = keyValue[1];

            switch (key) {
                case "iban":
                    this.iban = value;
                    break;
                case "primaryOwner":
                    this.primaryOwner = userManager.findCustomerByVAT(value);
                    break;
                case "dateCreated":
                    this.dateCreated = value;
                    break;
                case "rate":
                    this.rate = Double.parseDouble(value);
                    break;
                case "balance":
                    this.balance = Double.parseDouble(value);
                    break;
                case "coOwner":  // κάθε coOwner εμφανίζεται με ξεχωριστό key στο αρχείο
                    this.coOwner.add(userManager.findCustomerByVAT(value));
                    break;
            }
        }
    }



}
