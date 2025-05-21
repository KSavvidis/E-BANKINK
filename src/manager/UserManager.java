package manager;


import model.*;
import storage.FileStorageManager;
import storage.Storable;



import java.util.*;


public class UserManager {


    private final FileStorageManager storageManager = new FileStorageManager();
    private final List<User> users = new ArrayList<>();

    public UserManager() {
        loadUsers();
    }


    public void loadUsers() {
        Storable loader = new Storable() {

            @Override
            public String marshal() {
                return null;
            }

            @Override
            public void unmarshal(String data) {
                Map<String, String> map = new HashMap<>();
                String[] parts = data.split(",");

                for (String part : parts) {
                    String[] keyValuePair = part.split(":", 2);  // xwrizoume to key kai to value
                    if (keyValuePair.length == 2) {
                        String key = keyValuePair[0].trim();
                        String value = keyValuePair[1].trim();
                        map.put(key, value);
                    }
                }

                String type = map.get("type");
                if (type == null) return;

                try {
                    User user = null;
                    switch (type) {
                        case "Admin":
                            user = new Admin(
                                    map.get("userName"),
                                    map.get("password"),
                                    map.get("legalName"),
                                    map.get("type")
                            );
                            break;
                        case "Individual":
                            user = new Individual(
                                    map.get("userName"),
                                    map.get("password"),
                                    map.get("legalName"),
                                    map.get("type"),
                                    map.get("vatNumber")
                            );
                            break;
                        case "Company":
                            user = new Company(
                                    map.get("userName"),
                                    map.get("password"),
                                    map.get("legalName"),
                                    map.get("type"),
                                    map.get("vatNumber")
                            );
                            break;
                    }

                    if (user != null) users.add(user);

                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        };

        // fortwsi apo to arxeio xristwn
        storageManager.load(loader, "data/users/users.csv");
    }


    public User authenticate() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Username: ");
        String bufferUserName = sc.next();
        System.out.print("Password: ");
        String bufferPassword = sc.next();

        // Fortwnoume tous xristes apo to arxeio an den exoun idi fortwthei
        if (users.isEmpty()) {
            loadUsers();
        }

        for (User user : users) {
            if (user.getUserName().equals(bufferUserName) && user.getPassword().equals(bufferPassword)) {

                System.out.println("Authentication successful. Welcome, " + user.getLegalName() + "!");
                return user;
            }
        }

        System.out.println("Authentication failed. Invalid username or password.");
        return null;
    }

    public List<User> getAllUsers() {
        return users;
    }

    public void showCustomers(Scanner sc){
        if(users.isEmpty()) {
            loadUsers();
        }
        System.out.println("Customers");
        System.out.println("===================================");
        int i = 1;
        for(User user : users) {
            String type = user.getType();


            if(type.equals("Individual") || type.equals("Company")) {
                String legalName = user.getLegalName();
                String vatNumber = user.getVAT();
                System.out.printf("%2d. [%s]: %s (%s)\n",i,type,legalName,vatNumber);
                i++;
            }
        }
        System.out.println("Press any key to continue...");
        sc.nextLine();
    }

    public void showCustomerInfo(Scanner sc){

        if(users.isEmpty()) {
            loadUsers();
        }

        AccountManager accountManager = new AccountManager();
        String userName = "";
        String legalName = "";
        boolean found = false;
        System.out.println("Enter the VAT number of the user:");
        String userVatNumber = sc.next();
        sc.nextLine();
        for(User user : users) {
            if ("Company".equals(user.getType()) || "Individual".equals(user.getType())) {
                String bufferVatNumber = user.getVAT();
                if (bufferVatNumber.equals(userVatNumber)) {
                    legalName = user.getLegalName();
                    userName = user.getUserName();
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
                    boolean isPrimary = userVatNumber.equals(acc.getPrimaryOwner().getVAT());
                    boolean isCoOwner = false;
                    for(Customer coOwner: acc.getCoOwner()){
                        if(coOwner != null && coOwner.getVAT().equals(userVatNumber)){
                            isCoOwner = true;
                            break;
                        }
                    }
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

    public Customer findCustomerByVAT(String vatNumber) {
        for (User user : users) {
            if(user instanceof Customer && user.getVAT().equals(vatNumber)) {
                return (Customer)user;
            }
        }
        return null;
    }
}
