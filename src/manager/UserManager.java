package manager;

import menu.Menu;
import model.User;
import model.Individual;
import model.Admin;
import model.Company;
import storage.FileStorageManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;


public class UserManager {

    public String authenticate() {
        try (Scanner sc = new Scanner(System.in)) {
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
                    String userType = userMap.get("type");
                    String legalName = userMap.get("legalName");
                    System.out.println("Authentication successful. Welcome, " + legalName + "!");
                    return userType; // Epistrefei string
                }
            }

            System.out.println("Authentication failed. Invalid username or password.");
            return null;
        }
    }

    public User getUser(String type) {
        switch (type) {
            case "Individual":
                return new Individual("sampleUsername", "samplePassword", "Sample Legal Name", type, "sampleVAT");
            case "Admin":
                return new Admin("sampleUsername", "samplePassword", "Sample Legal Name", type, "sampleVAT");
            case "Company":
                return new Company("sampleUsername", "samplePassword", "Sample Legal Name", type, "sampleVAT");
            default:
                System.out.println("Invalid user type.");
                return null;
        }
    }



    private User createUser(String userName, String password,String legalName,String type,String VAT) {
        switch (type) {
            case "Individual":
                return new Individual(userName, password,legalName,type,VAT);
            case "Admin":
                return new Admin(userName, password,legalName,type,VAT);
            case "Company":
                return new Company(userName, password,legalName,type,VAT);
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
