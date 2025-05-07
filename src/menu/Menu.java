package menu;

import manager.UserManager;
import java.util.Scanner;
import model.User;

public class Menu {
    public void start(){
        System.out.println("Welcome to the TUC Bank Menu");
        System.out.println("===================================");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.println("===================================");
        try(Scanner sc = new Scanner(System.in)){
            int choice = sc.nextInt();
            switch(choice){
                case 1:
                    UserManager userManager = new UserManager();
                    User user = userManager.authenticate();
                    if (user != null) {
                        switch (user.getType()) {
                            case "Individual":
                                showIndividualMenu();
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

    private void showIndividualMenu(){
        System.out.println("Individual Customer Menu");
        System.out.println("======================");
        System.out.println("1. Overview");
        System.out.println("2. Withdraw");
        System.out.println("3. Deposit");
        System.out.println("4. Transfer");
        System.out.println("5. Create Standing Order");
        System.out.println("6. List Standing Orders");
        System.out.println("7. Back to main menu");
        System.out.println("8. Exit");
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

    private void showAdminMenu(){
    System.out.println("Admin Menu");
    }
    private void showCompanyMenu(){
    System.out.println("Company Menu");
    }
}
