package menu;

import manager.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import model.Account;
import model.PersonalAccount;
import model.User;
import model.Customer;
import storage.FileStorageManager;
import transaction.*;

public class Menu {

    public void start(){

        UserManager userManager = new UserManager();
        BillManager billManager = new BillManager();
        AccountManager accountManager = new AccountManager();
        StatementManager statementsManager = new StatementManager();
        statementsManager.initializeStatementFiles(accountManager.getAllAccounts());

        // Load data from storage
        userManager.loadUsers();
        System.out.println("✅ All users loaded.");

        accountManager.loadAccounts();
        System.out.println("✅ All accounts loaded.");

        statementsManager.initializeStatementFiles(accountManager.getAllAccounts());
        System.out.println("✅ Statements initialized for all accounts.");

        billManager.loadBillsForToday();  // <- θα πρέπει να έχεις ή να προσθέσεις αυτή τη μέθοδο στο BillManager
        System.out.println("✅ All bills loaded.");

        System.out.println("System initialization complete.\n");

        boolean exit = false;
        Scanner sc = new Scanner(System.in);

        while (!exit) {
            System.out.println("Welcome to the TUC Bank Menu");
            System.out.println("===================================");
            System.out.println("1. Login");
            System.out.println("2. Exit");
            System.out.println("===================================");
            if (sc.hasNextInt()) {
                int choice = sc.nextInt();
                sc.nextLine();
                switch (choice) {
                    case 1:
                        login(sc);
                        break;
                    case 2:
                        System.out.println("Exiting the system. Goodbye!");
                        exit = true;
                        sc.close();
                        break;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
            else {
                System.out.println("Invalid choice, please try again.");
                sc.next();
            }
        }
    }

    private void login(Scanner sc) {
        UserManager userManager = new UserManager();
        boolean exit = false;
        while (!exit) {
            User user = userManager.authenticate();//pairnei to type
            if (user != null) {
                switch (user.getType()) {//tsekarei to type
                    case "Individual":
                        exit = true;
                        showIndividualMenu(user, sc);
                        break;
                    case "Admin":
                        exit = true;
                        showAdminMenu(sc);
                        break;
                    case "Company":
                        exit = true;
                        showCompanyMenu(user,sc);
                        break;
                    default:
                        System.out.println("Unknown user type.");
                }
            } else {
                System.out.println("Please try again.");

            }
        }
    }
    private void showIndividualMenu(User user,Scanner sc) {
        boolean exit = false;
        while(!exit) {
            System.out.println("Individual Customer Menu");
            System.out.println("===================================");
            System.out.println("1. Overview");
            System.out.println("2. Transactions");
            System.out.println("3. Create Standing Order");
            System.out.println("4. List Standing Orders");
            System.out.println("5. Back to login screen");
            System.out.print("Enter your choice:");

            if (sc.hasNextInt()) {
                int choice = sc.nextInt();
                sc.nextLine(); // consume newline
                switch (choice) {
                    case 1:
                        showOverviewMenu(user, sc);
                        break;
                    case 2:
                        showTransactionsMenu(user, sc);
                        break;
                    case 3:
                        // Create Standing Order functionality
                        break;
                    case 4:
                        // List Standing Orders functionality
                        break;
                    case 5:
                        exit = true;
                        break;
                    default:
                        System.out.println("Invalid choice, please try again.");
                }
            }
            else {
                System.out.println("Please enter a valid choice. Try again.");
                sc.next(); // consume invalid input
            }
        }
    }

    private void showOverviewMenu(User user, Scanner sc) {
        AccountManager accountManager = new AccountManager();
        List<Account> userAccounts = accountManager.findByVat(user.getVAT());

        System.out.println("\nAccount Overview");
        System.out.println("===================================");
        System.out.println("Name: " + user.getLegalName());
        System.out.println("VAT: " + user.getVAT());
        System.out.println("\nYour Accounts:");

        if (userAccounts.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            for (Account acc : userAccounts) {
                String role = acc.getPrimaryOwner().equals(user.getVAT()) ? "Primary Owner" : "Co-Owner";
                System.out.printf("- IBAN: %s \t Balance: %.2f \t [%s]\n", acc.getIban(), acc.getBalance(), role);
            }
        }

        System.out.println("\nPress any key to continue...");
        sc.nextLine();
    }

    private void showTransactionsMenu(User user, Scanner sc) {
        AccountManager accountManager = new AccountManager();
        BillManager billManager = new BillManager();
        TransactionManager transactionManager = new TransactionManager(accountManager,billManager);

        if (!accountManager.hasAccounts(user.getVAT())) {
            System.out.println("You don't have any accounts to perform transactions on.");
            System.out.println("Press any key to continue...");
            sc.nextLine();
            return;
        }

        while (true) {
            System.out.println("\nTransactions Menu");
            System.out.println("===================================");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Transfer");
            System.out.println("4. Payment");
            System.out.println("5. Back to Individual Menu");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            sc.nextLine();

            if(choice == 5){
                return;
            }

            Transaction transaction = null;
            switch (choice) {
                case 1:
                    //transactionManager.deposit(selectedAccount.getIban(), sc);
                    transaction = new DepositTransaction(transactionManager);
                    break;
                case 2:
                    //transactionManager.performWithdraw(selectedAccount.getIban(), sc);
                    transaction = new WithdrawTransaction(transactionManager);
                    break;
                case 3:
                    //transactionManager.transfer(selectedAccount.getIban(), sc);
                    transaction = new TransferTransaction(transactionManager);
                    break;
                case 4:
                   // transactionManager.paymentOrder(selectedAccount.getIban(), selectedAccount.getPrimaryOwner(), sc);
                    transaction = new PaymentOrderTransaction(transactionManager);
                    break;
                default:
                    System.out.println("Invalid choice. Try again.");
                    continue;
            }
            Account selectedAccount = accountManager.selectAccountByUser(sc, user.getVAT());
            if (selectedAccount == null) {
                continue;
            }

            if (transaction != null) {
                transaction.execute(selectedAccount, sc);
            }
        }
    }

    private void showAdminMenu(Scanner sc){
        boolean exit = false;
        AccountManager accountManager = new AccountManager();
        BillManager billManager = new BillManager();
        TimeSimulator timeSimulator = new TimeSimulator(accountManager,billManager);
        while(!exit) {
            System.out.println("Admin Menu");
            System.out.println("===================================");
            System.out.println("1. Customers");
            System.out.println("2. Bank Accounts");
            System.out.println("3. Company Bills");
            System.out.println("5. List Standing Orders");
            System.out.println("6. Pay Customer's Bills");
            System.out.println("7. Simulate Time Passing");
            System.out.println("8. Back to login screen");
            System.out.print("Enter your choice:");

            int choice;
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        showCustomerMenu(sc);
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        simulateTimePassing(sc, timeSimulator);
                        break;
                    case 8:
                        exit = true;
                }
            } else {
                System.out.println("Please enter a valid choice. Try again.");
                sc.next();
            }
        }
    }

    private void simulateTimePassing(Scanner sc, TimeSimulator timeSimulator) {
        System.out.println("\nTime Simulation");
        System.out.println("===================================");
        System.out.println("Enter the target date (YYYY-MM-DD) to simulate until:");
        String dateInput = sc.next();
        sc.nextLine();
        try {
            LocalDate targetDate = LocalDate.parse(dateInput);
            timeSimulator.run(targetDate);
        }
        catch (Exception e) {
            System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
            System.out.println("Example: 2025-12-31");
        }
    }
    private void showCompanyMenu(User user,Scanner sc){
        boolean exit = false;

        while(!exit) {
            System.out.println("Company Menu");
            System.out.println("===================================");
            System.out.println("1. Overview");
            System.out.println("2. Bills");
            System.out.println("3. Back to login screen");
            System.out.print("Enter your choice:");

            int choice;
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                switch (choice) {
                    case 1:
                        showOverviewMenu(user, sc);
                        break;
                    case 2:
                        showBillMenu(user,sc);
                        break;
                    case 3:
                        exit = true;
                }
            } else {
                System.out.println("Please enter a valid choice. Try again.");
                sc.next();
            }
        }
    }

    private void showCustomerMenu(Scanner sc){
        UserManager userManager = new UserManager();
        while(true) {
            System.out.println("Customer Menu");
            System.out.println("===================================");
            System.out.println("1. List Customers");
            System.out.println("2. Print Customer Information");
            System.out.println("3. Update Customer");
            System.out.println("4. Delete Customer");
            System.out.println("5. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            if(sc.hasNextInt()){
                int choice = sc.nextInt();
                sc.nextLine();
                switch (choice) {
                    case 1:
                        userManager.showCustomers(sc);
                        break;
                    case 2:
                        userManager.showCustomerInfo(sc);
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        return;
                    default:
                        System.out.println("Invalid choice. Try again.");
                }
            }
            else {
                System.out.println("Please enter a valid choice. Try again.");
            }
        }
    }

    private void showBillMenu(User user,Scanner sc){
        BillManager billManager = new BillManager();
        AccountManager accountManager = new AccountManager();


        while(true) {
            System.out.println("Bill Menu");
            System.out.println("===================================");
            System.out.println("1. Load Issued Bills");
            System.out.println("2. Show Paid Bills");
            System.out.println("3. Back to Company Menu");
            System.out.print("Enter your choice:");
            int choice;
            if (sc.hasNextInt()) {
                choice = sc.nextInt();
                sc.nextLine();

                if(choice == 3){
                    return;
                }

                switch (choice) {
                    case 1:
                        billManager.loadIssuedBills(user.getVAT());
                        break;
                    case 2:
                        break;
                    default:
                        System.out.println("Invalid choice. Try again.");

                }
            }

        }
    }
}