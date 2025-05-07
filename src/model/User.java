package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.*;
import java.io.File;

public abstract class User {
    private String userName;
    private String password;
    private String legalName;
    private String type;

    private String authenticate() {
        File usersFile = new File("Users.csv");
        Scanner sc = new Scanner(System.in);
        System.out.println("Username: ");
        String bufferUserName = sc.next(); // onoma INput
        System.out.println("Password: ");
        String bufferPassword = sc.next();

        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, String> info = parseLine(line);
                String fileUsername = info.get("userName");
                String filePassword = info.get("password");

                if (bufferUserName.equals(fileUsername) && bufferPassword.equals(filePassword)) {
                    this.userName = fileUsername;
                    this.password = filePassword;
                    this.legalName = info.get("legalName");
                    this.type = info.get("type");
                    System.out.println("Authentication successful. Welcome, " + this.legalName + "!");
                    return this.type;
                }
            }
            System.out.println("Authentication failed: Invalid username or password.");
        } catch (Exception e) {
            System.out.println("Error:" + e.getMessage());
        }
        return null;
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