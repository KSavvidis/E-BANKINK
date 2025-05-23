package storage;

import java.io.*;

public class FileStorageManager implements StorageManager {

    @Override
    public void load(Storable s, String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                s.unmarshal(line);
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }


    @Override
    public void save(Storable s, String filePath, boolean append) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, append))) {
            String data = s.marshal();
            if (data != null) {
                writer.write(data);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving data: " + e.getMessage());
        }
    }

}


