package storage;

public interface Storable {
    String marshal();               // Μετατρέπει το αντικείμενο σε String (π.χ. για αποθήκευση)
    void unmarshal(String data);   // Φτιάχνει αντικείμενο από String (π.χ. ανάγνωση από αρχείο)
}