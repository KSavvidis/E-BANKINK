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


