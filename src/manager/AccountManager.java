package manager;

import model.*;
import storage.FileStorageManager;
import storage.Storable;

import java.util.*;

public class AccountManager {
    //lista gia apothikeusi twn account
    private final List<Account> accounts = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";
    private UserManager userManager = new UserManager();

    public AccountManager() {
        loadAccounts();
    }
    public AccountManager(UserManager userManager) {
        this.userManager = userManager;
        loadAccounts();//fortwnei account kata tin arxikopoiisi
    }

    public void loadAccounts() {
        Storable loader = new Storable() {
            @Override
            public String marshal() {
                return null;
            }

            @Override
            public void unmarshal(String data) {
                Map<String, String> map = new HashMap<>();
                List<Customer> coOwners = new ArrayList<>();
                String[] parts = data.split(",");
                for (String part : parts) {
                    String[] kv = part.split(":", 2);
                    if (kv.length == 2) {
                        String key = kv[0].trim();
                        String value = kv[1].trim();
                        if (key.equals("coOwner") && userManager.findCustomerByVAT(value) != null) {
                            coOwners.add(userManager.findCustomerByVAT(value));
                        } else {
                            map.put(key, value);
                        }
                    }
                }

                String type = map.get("type");
                if (type == null) return;

                Account acc = null;
                try {

                    if (type.equals("PersonalAccount")) {
                        acc = new PersonalAccount(
                                map.get("iban"),
                                userManager.findCustomerByVAT(map.get("primaryOwner")),
                                coOwners,
                                map.get("dateCreated"),
                                Double.parseDouble(map.get("rate")),
                                Double.parseDouble(map.get("balance"))
                        );
                    } else if (type.equals("BusinessAccount")) {
                        acc = new BusinessAccount(
                                map.get("iban"),
                                userManager.findCustomerByVAT(map.get("primaryOwner")),
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
            new java.io.PrintWriter(accountsFilePath).close(); // clear//ti einai auto re malaka
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
                Customer primaryOwner = account.getPrimaryOwner();
                if (primaryOwner != null && primaryOwner.getVAT().equals(vat)) {
                    userAccounts.add(account);
                }
                else if (account instanceof PersonalAccount) {
                    List<Customer> coOwners = account.getCoOwner();
                    if(coOwners != null) {
                        for(Customer coOwner : coOwners) {
                            if(coOwner.getVAT().equals(vat)) {
                                userAccounts.add(account);
                            }
                        }
                    }
                }
            }
            return userAccounts;
        }

    public Account selectAccountByUser(Scanner sc, String vat) {
        List<Account> allAccounts = findByVat(vat);

        if (allAccounts.isEmpty()) {
            System.out.println("No accounts found.");
            return null;
        }

        System.out.println("Select an account:");
        for (int i = 0; i < allAccounts.size(); i++) {
            Account acc = allAccounts.get(i);
            String role = acc.getPrimaryOwner().getVAT().equals(vat) ? "Primary Owner" : "Co-Owner";
            System.out.printf("%d. IBAN: %s \t Balance: %.2f \t [%s]\n", i + 1, acc.getIban(), acc.getBalance(), role);
        }

        System.out.printf("Enter account number (1-%d): ", allAccounts.size());
        int index = sc.nextInt();
        sc.nextLine();

        if (index < 1 || index > allAccounts.size()) {
            System.out.println("Invalid selection.");
            return null;
        }

        return allAccounts.get(index - 1);
    }

    public List<Account> findCoOwnedAccounts(String vat) {
        List<Account> coOwnedAccounts = new ArrayList<>();

        for (Account account : accounts) {
            if (account instanceof PersonalAccount) {
                // Έλεγχος αν ο χρήστης είναι co-owner
                if (account.getCoOwner() != null && account.getCoOwner().contains(vat)) {
                    coOwnedAccounts.add(account);
                }
            }
        }
        return coOwnedAccounts;
    }

    public void createAccountOfBank(){
        String iban = generateIBAN();
        BankAccount bank = new BankAccount(iban, 69696969, "Bank");
        bank.matchBankAccount(bank);
    }

    public String generateIBAN(){
        String iban = "";
        Random r = new Random();
        iban += "GR" + "200";
        for(int i=0; i<15; i++) {
            iban += r.nextInt(10);

        }
        System.out.println("IBAN: " + iban);
        return iban;
    }
    public boolean hasAccounts(String vat) {
        return !findByVat(vat).isEmpty();
    }
}
