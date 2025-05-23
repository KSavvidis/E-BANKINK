package manager;

import model.*;
import storage.FileStorageManager;
import storage.Storable;
import transaction.TransferTransaction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.LocalDate;
import java.util.*;

public class AccountManager {
    private final List<Account> accounts = new ArrayList<>();
    private final FileStorageManager storageManager = new FileStorageManager();
    private final String accountsFilePath = "data/accounts/accounts.csv";
    private UserManager userManager = new UserManager();

    public AccountManager() {
        loadAccounts();
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
                    }
                    else if (type.equals("BusinessAccount")) {
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
        if(BankAccount.getInstance() == null) {
            createAccountOfBank();
        }
        storageManager.load(loader, accountsFilePath);

    }

    public List<Account> getAllAccounts() {
       return accounts;
    }

    public Account findByIban(String iban) {
        for (Account acc : accounts) {
            if (acc.getIban().equals(iban)) return acc;
        }
        return null;
    }
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
        if(!sc.hasNextInt()) {
            System.out.println("Invalid input. Try again.");
            return null;
        }
        int index = sc.nextInt();
        sc.nextLine();

        if (index < 1 || index > allAccounts.size()) {
            System.out.println("Invalid selection.");
            return null;
        }

        return allAccounts.get(index - 1);
    }

    public void createAccountOfBank(){
        String iban = generateIBAN();
        BankAccount bank = new BankAccount(iban, 69696969, "TUC");
        bank.matchBankAccount(bank);
        accounts.add(bank);
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

    public void printAccountsForMenu(Scanner sc) {
        System.out.println("Bank Accounts: ");
        int i = 1;
        for(Account acc : accounts) {
            System.out.printf("%d. IBAN: %s \t Balance: %.2f \t VatNumber of PrimaryOwner: %s\n", i, acc.getIban(), acc.getBalance(), acc.getPrimaryOwner().getVAT());
            i++;
        }
        System.out.println("Press any key to continue...");
        sc.nextLine();
    }

    public void showAccountInfo(Scanner sc) {
        System.out.println("Please Insert the Bank Account's IBAN: ");
        String iban = sc.next();
        sc.nextLine();
        Account acc = findByIban(iban);
        if (acc != null) {
            System.out.println("Details for Account: " + acc.getIban());
            System.out.println("----------------------------------------");
            System.out.printf("Balance: %.2f \t PrimaryOwner: [%s]",acc.getBalance(), acc.getPrimaryOwner().getVAT());
            if(acc instanceof PersonalAccount && !acc.getCoOwner().isEmpty()) {
                for(Customer coOwner : acc.getCoOwner()) {
                    System.out.printf("\t Co-Owner: [%s]", coOwner.getVAT());
                }
            }
            System.out.printf("\t Date Created: %s\n", acc.getDateCreated());
            System.out.println("----------------------------------------");
            System.out.println("Press any key to continue...");
            sc.nextLine();
        }
        else {
            System.out.println("Invalid IBAN. Please try again.");
        }
    }

    public void showAccountStatements(Scanner sc) {
        System.out.println("Please Insert the Bank Account's IBAN: ");
        String iban = sc.next();
        sc.nextLine();
        Account acc = findByIban(iban);
        if (acc != null) {
            String statementsFilePath = "data/statements/" + acc.getIban() + ".csv";
            File statementsFile = new File(statementsFilePath);

            if(!statementsFile.exists() || statementsFile.length() == 0) {
                System.out.println("No statements available for this IBAN: +" + iban);
                System.out.println("Press any key to continue...");
                sc.nextLine();
                return;
            }
            System.out.println("Account Statements for IBAN: " + iban);
            System.out.println("----------------------------------------");
            try(BufferedReader br = new BufferedReader(new FileReader(statementsFile))){
                String line;

                while((line = br.readLine()) != null) {
                    System.out.println(line);
                }
            }
            catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
            System.out.println("----------------------------------------");
            System.out.println("Press any key to continue...");
            sc.nextLine();
        }
        else {
            System.out.println("Invalid IBAN. Please try again.");
            System.out.println("Press any key to continue...");
            sc.nextLine();
        }
    }

    public Map<Account, Double> applyRate(LocalDate currentDate, Map<Account, Double> monthlyRate, TransactionManager transactionManager){
        BankAccount bankAccount = BankAccount.getInstance();
        for(Account account : accounts) {
            double dailyInterest = account.getBalance() * account.getRate()/365.0;
            if(monthlyRate.containsKey(account)){
                monthlyRate.put(account, monthlyRate.get(account) + dailyInterest);
            }
            else {
                monthlyRate.put(account, dailyInterest);
            }
            if(currentDate.getDayOfMonth() == currentDate.lengthOfMonth()) {
                if(monthlyRate.get(account) > 0){
                    TransferTransaction transferRate = new TransferTransaction(transactionManager);
                    transferRate.execute(bankAccount, account, monthlyRate.get(account), "Monthly Rate");
                    monthlyRate.put(account, 0.0);
                }
            }
        }
        return monthlyRate;
    }

    public void applyFee(LocalDate currentDate, TransactionManager transactionManager) {
        BankAccount bankAccount = BankAccount.getInstance();

        if(currentDate.getDayOfMonth() == currentDate.lengthOfMonth()) {
            for(Account account : accounts) {
                if(account instanceof BusinessAccount){
                    BusinessAccount businessAccount = (BusinessAccount) account;
                    if(account.getBalance() >= businessAccount.getFee()){
                        TransferTransaction transferFee = new TransferTransaction(transactionManager);
                        transferFee.execute(account, bankAccount, businessAccount.getFee(), "Monthly Fee");
                    }
                }
            }
        }
    }
}
