package manager;

import menu.Menu;
import model.*;
import storage.FileStorageManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class UserManager {

    private String fileUserName;
    private String filePassword;
    private String fileLegalName;
    private String fileType;
    private String fileVAT;

    public User authenticate() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String bufferUserName = sc.next();
        System.out.print("Password: ");
        String bufferPassword = sc.next();

        // ftiaxnetai antikeimneo FileStorageManager gia na diavasei dedomena apo to arxeio
        FileStorageManager storageManager = new FileStorageManager();
        //fortwnei ta dedomena apo to users.csv
        List<Map<String, String>> userData = storageManager.getUsersFromFile("data/users/users.csv");

        for (Map<String, String> userMap : userData) {
            String fileUsername = userMap.get("userName");
            String filePassword = userMap.get("password");

            if (bufferUserName.equals(fileUsername) && bufferPassword.equals(filePassword)) {
                this.fileUserName = fileUsername;
                this.filePassword = filePassword;
                this.fileLegalName = userMap.get("legalName");
                this.fileType = userMap.get("type");
                this.fileVAT = userMap.get("vatNumber");
                System.out.println("Authentication successful. Welcome, " + fileLegalName + "!");
                if(fileType.equals("Admin")) {
                    return createUser(fileUserName, filePassword, fileLegalName, fileType, null);
                }
                return createUser(fileUserName, filePassword, fileLegalName, fileType, fileVAT);
            }
        }
        System.out.println("Authentication failed. Invalid username or password.");
        return null;
    }

    public User getUser(String type) {
        switch (type) {
            case "Individual":
                return new Individual(fileUserName, filePassword, fileLegalName, fileType, fileVAT);
            case "Admin":
                return new Admin(fileUserName, filePassword, fileLegalName, fileType);
            case "Company":
                return new Company(fileUserName, filePassword, fileLegalName, fileType, fileVAT);
            default:
                System.out.println("Invalid user type.");
                return null;
        }
    }

    public void showCustomers(Scanner sc){
        FileStorageManager storageManager = new FileStorageManager();
        List<Map<String, String>> userData = storageManager.getUsersFromFile("data/users/users.csv");
        System.out.println("Customers");
        System.out.println("===================================");
        int i = 0;
        for(Map<String, String> userMap : userData) {
            String type = userMap.get("type");

            if(type.equals("Individual") || type.equals("Company")){
                String legalName = userMap.get("legalName");
                String vatNumber = userMap.get("vatNumber");
                System.out.printf("%2d. [%s]: %s (%s)\n",i,type,legalName,vatNumber);
                i++;
            }
        }
        System.out.println("Press any key to continue...");
        sc.nextLine();
    }

    public void showCustomerInfo(Scanner sc){
        FileStorageManager storageManager = new FileStorageManager();
        List<Map<String, String>> userData = storageManager.getUsersFromFile("data/users/users.csv");
        AccountManager accountManager = new AccountManager();
        String userName = "";
        String legalName = "";
        boolean found = false;
        System.out.println("Enter the VAT number of the user:");
        String userVatNumber = sc.next();
        sc.nextLine();
        for(Map<String, String> userMap : userData) {
            if ("Company".equals(userMap.get("type")) || "Individual".equals(userMap.get("type"))) {
                String bufferVatNumber = userMap.get("vatNumber");
                if (bufferVatNumber.equals(userVatNumber)) {
                    legalName = userMap.get("legalName");
                    userName = userMap.get("userName");
                    found = true;
                    break;
                }
            }
        }
        if(!found){
            System.out.println("VAT number not found.");
            return;
        }
        System.out.println("Customer Information:");
        System.out.println("------------------------------------");
        System.out.println("Legal Name: " + legalName);
        System.out.println("VAT number: " + userVatNumber);
        System.out.println("Username: " + userName);
        System.out.println("------------------------------------");
        if(accountManager.getAllAccounts().isEmpty()){
            System.out.println("No accounts found.");
        }
        else {
            List<Account> allAccounts = accountManager.getAllAccounts();
            for (Account acc : allAccounts) {
                    boolean isPrimary = userVatNumber.equals(acc.getPrimaryOwner());
                    boolean isCoOwner = acc.getCoOwner() != null && acc.getCoOwner().contains(userVatNumber);

                    if (isPrimary || isCoOwner) {
                        String role = isPrimary ? "Primary Owner" : "Co-Owner";
                        System.out.printf(" IBAN: %-22s Balance: %10.2f  [%s]\n", acc.getIban(), acc.getBalance(), role);
                    }
                }

            System.out.println("------------------------------------");
        }
        System.out.println("Press any key to continue...");
        sc.nextLine();
    }


    private User createUser(String userName, String password, String legalName, String type, String VAT) {
        switch (type) {
            case "Individual":
                return new Individual(userName, password, legalName, type, VAT);
            case "Admin":
                return new Admin(userName, password, legalName, type);
            case "Company":
                return new Company(userName, password, legalName, type, VAT);
            default:
                return null;
        }
    }

    private Map<String, String> parseLine(String line) {
        Map<String, String> map = new HashMap<>();
        String[] parts = line.split(",");
        for (String part : parts) {
            String[] keyValue = part.split(":");
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }
}
