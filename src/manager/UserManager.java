package manager;

import model.User;
import model.Individual;
import model.Admin;
import model.Company;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class UserManager {

    public User authenticate() {
        File usersFile = new File("Users.csv");
        Scanner sc = new Scanner(System.in);
        System.out.println("Username: ");
        String bufferUserName = sc.next(); // όνομα input
        System.out.println("Password: ");
        String bufferPassword = sc.next();

        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, String> info = parseLine(line);
                String fileUsername = info.get("userName");
                String filePassword = info.get("password");

                if (bufferUserName.equals(fileUsername) && bufferPassword.equals(filePassword)) {
                    String userType = info.get("type");
                    User user = createUser(userType, fileUsername, filePassword);
                    user.setLegalName(info.get("legalName"));
                    System.out.println("Authentication successful. Welcome, " + user.getLegalName() + "!");
                    return user;
                }
            }
            System.out.println("Authentication failed: Invalid username or password.");
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return null;
    }

    private User createUser(String userName, String password,String legalName,String type,String VAT) {
        switch (userType) {
            case "Individual":
                return new Individual(userName, password,legalName,type,VAT);
            case "Admin":
                return new Admin(userName, password,legalN);
            case "Company":
                return new Company(userName, password);
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
