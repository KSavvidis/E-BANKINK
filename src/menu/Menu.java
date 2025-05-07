package menu;

import manager.UserManager;
import java.util.Scanner;

public class Menu {
    private void start(){
        System.out.println("Welcome to the TUC Bank Menu");
        System.out.println("===================================");
        System.out.println("1. Login");
        System.out.println("2. Exit");
        System.out.println("===================================");
        try(Scanner sc = new Scanner(System.in)){
            int choice = sc.nextInt();
            switch(choice){
                case 1:

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
    System.out.println("========================");
    System.out.println("1. Customers");
    System.out.println("2. Bank Accounts");
    System.out.println("3. Bills");
    System.out.println("4. Transactions");
    System.out.println("5. Time simulation");
    System.out.println("6. Exit");
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
    }
    private void showCompanyMenu(){

    }
}
