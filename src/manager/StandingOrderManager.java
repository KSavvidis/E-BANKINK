package manager;

import model.StandingOrder;

import java.time.LocalDate;
import java.util.Scanner;

public class StandingOrderManager {

    public StandingOrderManager() {}
    
    public void createStandingOrder(Scanner sc){
        System.out.println("Creating standing order");
        System.out.println("===============================");
        System.out.println("Insert Standing Order Name: ");
        String standingOrderName = sc.next();
        sc.nextLine();
        System.out.println("Insert the Date until the Standing Order is valid: ");
        String dateInput = sc.next();
        sc.nextLine();
        LocalDate standingOrderDate = null;
        boolean validDate = false;
        while(!validDate) {
            try {
                standingOrderDate = LocalDate.parse(dateInput);
            } catch (Exception e) {
                System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
            }
            if(standingOrderDate != null) {
                validDate = true;
            }
        }
        System.out.println("Insert the Standing Order Description: ");
        String standingOrderDescription = sc.next();
        sc.nextLine();
        try{
            StandingOrder standingOrder = new StandingOrder(standingOrderName,standingOrderDate,standingOrderDescription);
        }
        catch (Exception e) {
            System.out.println("Error while creating standing order: " + e.getMessage());
        }
        System.out.println("Standing Order Created.");
    }
}
