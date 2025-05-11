package model;

import java.util.List;

public class PersonalAccount extends Account {
    public PersonalAccount(String iban, String primaryOwner, List<String> coOwner, String dateCreated, double rate, double balance) {
        super(iban, primaryOwner,coOwner, dateCreated, rate, balance);
    }
    @Override
    public String marshal() {
        StringBuilder sb = new StringBuilder();
        sb.append("type:PersonalAccount");
        sb.append(",iban:").append(iban);
        sb.append(",primaryOwner:").append(primaryOwner);
        sb.append(",dateCreated:").append(dateCreated);
        sb.append(",rate:").append(String.format("%.2f", rate));
        sb.append(",balance:").append(String.format("%.2f", balance));

        if (coOwner != null && !coOwner.isEmpty()) {
            sb.append(",coOwners:").append(String.join(";", coOwner));
        }

        return sb.toString();
    }

    @Override
    public void unmarshal(String data) {

    }
}
