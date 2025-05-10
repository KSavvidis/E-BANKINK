package manager;

import model.Account;
import model.BusinessAccount;
import model.PersonalAccount;
import storage.FileStorageManager;
import storage.Storable;

import java.util.*;

public class AccountManager {
    //lista gia apothikeusi twn account
    private final List<Account> accounts = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";

    public AccountManager() {
        loadAccounts();//fortwnei account kata tin arxikopoiisi
    }

    public void loadAccounts() {
        Storable loader = new Storable() {
            @Override
            public String marshal() { return null; }

            //metatrepei tin grammi tou arxeiou(string) se object
            @Override
            public void unmarshal(String data) {
                Map<String, String> map = new HashMap<>();
                String[] parts = data.split(",");
                for (String part : parts) {
                    String[] kv = part.split(":", 2);
                    if (kv.length == 2) map.put(kv[0].trim(), kv[1].trim());
                }

                String type = map.get("type");
                if (type == null) return;

                Account acc = null;
                try {//dimiourgei i personal i business account
                    if (type.equals("PersonalAccount")) {
                        acc = new PersonalAccount(
                                map.get("iban"),
                                map.get("primaryOwner"),
                                map.get("dateCreated"),
                                Double.parseDouble(map.get("rate")),//pairnei tin timi apo to map me kleidi rate pou einai string kai tin metattrepei se double
                                Double.parseDouble(map.get("balance"))
                        );
                    } else if (type.equals("BusinessAccount")) {
                        acc = new BusinessAccount(
                                map.get("iban"),
                                map.get("primaryOwner"),
                                map.get("dateCreated"),
                                Double.parseDouble(map.get("rate")),
                                Double.parseDouble(map.get("balance")),
                                Double.parseDouble(map.get("fee"))
                        );
                    }
                } catch (Exception e) {
                    System.out.println("Error parsing account: " + e.getMessage());
                }

                if (acc != null) accounts.add(acc);
            }
        };

        storageManager.load(loader, accountsFilePath);
    }

    //apothikevei oloous tous logariasmous sto arxeio me xrisi tis save apo filestoragemanager
    public void saveAccounts() {
        try {
            new java.io.PrintWriter(accountsFilePath).close(); // clear
        } catch (Exception e) {
            System.out.println("Error clearing file: " + e.getMessage());
        }

        for (Account acc : accounts) {
            storageManager.save(acc, accountsFilePath, true);
        }
    }

    public List<Account> getAllAccounts() {
       return accounts;
    }

    //tha to xreiasteis esu gia tis alles duo methodous
    public Account findByIban(String iban) {
        for (Account acc : accounts) {
            if (acc.getIban().equals(iban)) return acc;
        }
        return null;
    }
    //vriskei account me vasi vat alla to apothikevei se primaryowner kathws vat==primaryowner sto neo arxeio
    public Account findByVat(String vat) {
        for (Account acc : accounts) {
            if (acc.getPrimaryOwner().equals(vat)) {
                return acc;
            }
        }
        return null;
    }


}
