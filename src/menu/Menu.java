package menu;

import java.util.Scanner;

public class Menu {
    private void start(){

    }

    private void showIndividualMenu(){
        System.out.println("1.Overview");
        System.out.println("2.Withdraw");
        System.out.println("3.Deposit");
        System.out.println("4.Transfer");
        System.out.println("5.Create Standing Order");
        System.out.println("6.List Standing Orders");
        System.out.println("7.Back to main menu");
        System.out.println("8.Exit");
        System.out.print("Enter your choice:");
        try(Scanner sc = new Scanner(System.in)){
            if(sc.hasNextInt()){
                int choice = sc.nextInt();
                switch(choice){
                    case 1:

                }
            }
        }
        catch(Exception e){
            System.out.println("Error:" + e.getMessage());
        }
    }

    private void showAdminMenu(){

    }
    private void showCompanyMenu(){

    }
}
