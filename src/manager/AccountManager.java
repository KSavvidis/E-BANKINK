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
        Storable loader = new Storable() {//giati den ginetai implement ths storable den exw katalabei kai ginetai auto
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
                        List<String> coOwners = new ArrayList<>();
                        if(map.containsKey("coOwners")) {
                            String[] owners = map.get("coOwner").split(":");
                            coOwners = Arrays.asList(owners);
                            }
                        acc = new PersonalAccount(
                                map.get("iban"),
                                map.get("primaryOwner"),
                                coOwners,
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
    public List<Account> findByVat(String vat) {
            List<Account> userAccounts = new ArrayList<>();
            for (Account account : accounts) {
                if (account.getPrimaryOwner().equals(vat)) {
                    userAccounts.add(account);
                }
                else if (account instanceof PersonalAccount && account.getCoOwner().contains(vat)) {
                    userAccounts.add(account);
                }
            }
            return userAccounts;
        }

    public Account selectAccountByUser(Scanner sc, String vat) {
        List<Account> userAccounts = findByVat(vat);

        if (userAccounts.isEmpty()) {
            System.out.println("You don't have any accounts.");
            return null;
        }

        System.out.println("\nSelect an account:");
        for (int i = 0; i < userAccounts.size(); i++) {
            Account acc = userAccounts.get(i);
            String role = acc.getPrimaryOwner().equals(vat) ? "Primary Owner" : "Co-Owner";
            System.out.printf("%d. %s (Balance: %.2f) [%s]\n",
                    i + 1, acc.getIban(), acc.getBalance(), role);
        }

        System.out.print("Enter account number (1-" + userAccounts.size() + "): ");
        int choice = sc.nextInt() - 1;
        sc.nextLine(); // Consume newline

        if (choice < 0 || choice >= userAccounts.size()) {
            System.out.println("Invalid account selection.");
            return null;
        }

        return userAccounts.get(choice);
    }

    public boolean hasAccounts(String vat) {
        return !findByVat(vat).isEmpty();
    }
}
