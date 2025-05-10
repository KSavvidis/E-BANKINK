package storage;

import java.io.*;
import java.util.*;

public class FileStorageManager implements StorageManager {

    @Override
    public void load(Storable s, String filePath) {//exei string filepath gia na xrhsimopoihuei kai se alles methodous oxi mono authenticate
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                s.unmarshal(line);  //kathe grammi analuetai apo to antikeimno s tupou storable
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public List<Map<String, String>> getUsersFromFile(String filePath) {
        List<Map<String, String>> users = new ArrayList<>();

        Storable userLoader = new Storable() {

            @Override
            public String marshal() {
                return null; // de mas noiazei giati kanei apo object se string
            }

            @Override
            public void unmarshal(String data) {
                Map<String, String> map = new HashMap<>();
                String[] parts = data.split(",");  //xwrizei tin grammi


                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];  //pairnei kathe meros tis grammis

                    String[] keyValuePair = part.split(":", 2);  // xwrizei to key kai to value

                    if (keyValuePair.length == 2) {
                        String key = keyValuePair[0].trim();
                        String value = keyValuePair[1].trim();
                        map.put(key, value);
                    }
                }

                users.add(map);  // prosthetei ton xarti stin lista twnn user
            }

        };

        load(userLoader, filePath);

        return users;
    }

    @Override
    public void save(Storable s, String filePath, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            // Χρησιμοποιούμε την marshal() για να μετατρέψουμε το αντικείμενο σε String
            String data = s.marshal();

            if (data != null) {
                writer.write(data);
                writer.newLine(); // Προσθέτει νέα γραμμή μετά από κάθε δεδομένο
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

}


