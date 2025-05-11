package menu;

import manager.UserManager;


import java.util.List;
import java.util.Scanner;

import model.Account;
import model.PersonalAccount;
import model.User;
import manager.TransactionManager;
import manager.AccountManager;
import model.Customer;

public class Menu {

    public void start(){
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
        User user = userManager.authenticate();//pairnei to type
        if (user != null) {
            switch (user.getType()) {//tsekarei to type
                case "Individual":
                    showIndividualMenu(user, sc);
                    break;
                case "Admin":
                    showAdminMenu(user, sc);
                    break;
                case "Company":
                    showCompanyMenu(sc);
                    break;
                default:
                    System.out.println("Unknown user type.");
            }
        }
        else {
            System.out.println("Please try again.");
            start();
        }
    }
    private void showIndividualMenu(User user,Scanner sc) {
        boolean exit = false;
        while(!exit) {
            System.out.println("Individual Customer Menu");
            System.out.println("======================");
            System.out.println("1. Overview");
            System.out.println("2. Transactions");
            System.out.println("3. Create Standing Order");
            System.out.println("4. List Standing Orders");
            System.out.println("5. Back to main menu");
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

    private void showOverviewMenu(User user,Scanner sc) {
        AccountManager accountManager = new AccountManager();
        List<Account> accounts = accountManager.getAllAccounts();
        System.out.println("1. Overview");
        System.out.println("======================");
        System.out.println("Name: " + user.getLegalName());
        System.out.println("VAT: " + user.getVAT());
        for (Account acc : accounts) {
            String role = null;
            if (acc.getPrimaryOwner().equals(user.getVAT())) {
                role = "Primary Owner";
            }
            else if (acc instanceof PersonalAccount) {
                if (acc.getCoOwner().contains(user.getVAT())) {
                    role = "Co-Owner";
                }
            }
            if (role != null) {
                System.out.printf("Account: %s \t Balance: %.2f \t [%s]\n", acc.getIban(), acc.getBalance(), role);
            }
        }
        System.out.println("Press any key to continue...");
        String key = sc.next();
    }
    private void showTransactionsMenu(User user, Scanner sc) {
        AccountManager accountManager = new AccountManager();
        TransactionManager transactionManager = new TransactionManager(accountManager);

        while (true) {
            System.out.println("\nTransactions Menu");
            System.out.println("======================");
            System.out.println("1. Deposit");
            System.out.println("2. Withdraw");
            System.out.println("3. Back to main menu");
            System.out.print("Enter your choice: ");

            int choice = sc.nextInt();
            switch (choice) {
                case 1:
                    System.out.print("Enter amount to deposit: ");
                    double depositAmount = sc.nextDouble();
                    transactionManager.deposit(user.getVAT(), depositAmount); // Access VAT from Customer
                    break;
                case 2:
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = sc.nextDouble();
                    transactionManager.withdraw(user.getVAT(), withdrawAmount); // Access VAT from Customer
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private void showAdminMenu(User user, Scanner sc){
        System.out.println("Admin Menu");
        System.out.println("======================");
        System.out.println("1. Customers");
        System.out.println("2. Bank Accounts");
        System.out.println("3. Company Bills");
        System.out.println("5. List Standing Orders");
        System.out.println("6. Pay Customer's Bills");
        System.out.println("7. Simulate Time Passing");
        System.out.println("8. Back to main menu");
        System.out.print("Enter your choice:");

        int choice;
        if(sc.hasNextInt()){
            choice = sc.nextInt();
            switch(choice){
                case 1:

            }
        }
        else {
            System.out.println("Please enter a valid choice. Try again.");
            sc.next();
            showAdminMenu(user, sc);
        }
    }

    private void showCompanyMenu(Scanner sc){
        System.out.println("Company Menu");
    }
}
