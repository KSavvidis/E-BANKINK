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
