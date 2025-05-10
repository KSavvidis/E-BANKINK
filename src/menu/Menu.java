package menu;

import manager.UserManager;
import java.util.Scanner;
import model.User;
import manager.TransactionManager;
import manager.AccountManager;
import model.Customer;

public class Menu {
    public void start(){
        System.out.println("Welcome to the TUC Bank Menu");
        System.out.println("===================================");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.println("===================================");
        try(Scanner sc = new Scanner(System.in)){
            int choice = sc.nextInt();
            String type;//nea metavliti gia to type
            User user=null;
            switch(choice){
                case 1:
                    UserManager userManager = new UserManager();
                    type = userManager.authenticate();//pairnei to type
                    if (type != null) {
                        user = userManager.getUser(type);
                        switch (type) {//tsekarei to type
                            case "Individual":
                                showIndividualMenu(user);
                                break;
                            case "Admin":
                                showAdminMenu();
                                break;
                            case "Company":
                                showCompanyMenu();
                                break;
                            default:
                                System.out.println("Unknown user type.");
                        }
                    }
                    break;
                case 2:
                    System.out.println("Exiting the system. Goodbye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid choice, please try again.");
                    start();
            }
        }
    }

    private void showIndividualMenu(User user){
        boolean exit=false;
        try(Scanner sc = new Scanner(System.in)){
            while (!exit) {
                System.out.println("Individual Customer Menu");
                System.out.println("======================");
                System.out.println("1. Overview");
                System.out.println("2. Transactions");
                System.out.println("3. Create Standing Order");
                System.out.println("4. List Standing Orders");
                System.out.println("5. Back to main menu");
                System.out.print("Enter your choice:");

                int choice;
                if (sc.hasNextInt()) {
                    choice = sc.nextInt();
                    switch (choice) {
                        case 2:
                            showTransactionsMenu(user);
                            break;

                    }
                } else {
                    System.out.println("Please enter a valid choice. Try again.");
                }
            }
        }
        catch(Exception e){
            System.out.println("Error:" + e.getMessage());
        }
    }

    private void showTransactionsMenu(User user) {
        AccountManager accountManager = new AccountManager();
        TransactionManager transactionManager = new TransactionManager(accountManager);

        Scanner sc = new Scanner(System.in);

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
                    transactionManager.deposit(((Customer) user).getVAT(), depositAmount); // Access VAT from Customer
                    break;
                case 2:
                    System.out.print("Enter amount to withdraw: ");
                    double withdrawAmount = sc.nextDouble();
                    transactionManager.withdraw(((Customer) user).getVAT(), withdrawAmount); // Access VAT from Customer
                    break;
                case 3:
                    return;
                default:
                    System.out.println("Invalid choice. Try again.");
            }
        }
    }


    private void showAdminMenu(){
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
        try(Scanner sc = new Scanner(System.in)){
            if(sc.hasNextInt()){
                int choice = sc.nextInt();
                switch(choice){
                    case 1:

                }
            }
            else{
                System.out.println("Please enter a valid choice. Try again.");
            }
        }
        catch(Exception e){
            System.out.println("Error:" + e.getMessage());
        }
    }
    private void showCompanyMenu(){
        System.out.println("Company Menu");
    }
}
