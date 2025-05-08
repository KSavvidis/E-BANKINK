package storage;

public interface StorageManager {
    void load(Storable s, String filePath);
    void save(Storable s, String filePath, boolean append);
}
