import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// Added imports for encryption 
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

public class InventoryDatabase {
    private List<Product> products;
    private List<String> activityLogs; 
    private String currentUserFile = "inventory_data.csv"; 
    
    // Maps username to [password, fullName, email, personalNotes, profilePicPath]
    private Map<String, String[]> users; 
    private final String USERS_FILE = "users_data.csv";

    // SECURITY PROTOCOL (AES ENCRYPTION) 
    private static final String ALGO = "AES";
    private static final byte[] keyValue = new byte[] { 'S', 'm', 'a', 'r', 't', 'I', 'n', 'v', 'K', 'e', 'y', '1', '2', '3', '4', '5' }; // 16-byte secret key

    private String encrypt(String data) {
        try {
            Key key = new SecretKeySpec(keyValue, ALGO);
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.ENCRYPT_MODE, key);
            byte[] encVal = c.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encVal);
        } catch (Exception e) {
            return data; 
        }
    }

    private String decrypt(String encryptedData) {
        try {
            Key key = new SecretKeySpec(keyValue, ALGO);
            Cipher c = Cipher.getInstance(ALGO);
            c.init(Cipher.DECRYPT_MODE, key);
            byte[] decodedValue = Base64.getDecoder().decode(encryptedData);
            byte[] decValue = c.doFinal(decodedValue);
            return new String(decValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return encryptedData; 
        }
    }

    public InventoryDatabase() {
        products = new ArrayList<>();
        activityLogs = new ArrayList<>();
        users = new HashMap<>();
        
        loadUsersFromFile(); 
        logActivity("System Initialized.");
    }

    // Sets the active user and automatically loads their specific data file
    public void setCurrentUser(String username) {
        this.currentUserFile = "inventory_" + username + ".csv";
        this.products.clear();
        this.activityLogs.clear();
        logActivity("Session securely started for user: " + username);
        try {
            loadFromFile();
        } catch (IOException e) {
            // First time login, file doesn't exist yet, which is completely fine.
        }
    }

public boolean registerUser(String username, String password, String fullName, String email, String phone) {
    if (users.containsKey(username)) return false; 
    // Added phone to the end of the array (index 5)
    users.put(username, new String[]{password, fullName, email, "", "", phone});
    saveUsersToFile();
    return true;
}

    public boolean authenticateUser(String username, String password) {
        return users.containsKey(username) && users.get(username)[0].equals(password);
    }

    public boolean verifyUserEmail(String username, String email) {
        if (users.containsKey(username)) {
            return users.get(username)[2].equalsIgnoreCase(email);
        }
        return false;
    }

    public void updatePassword(String username, String newPassword) {
        if (users.containsKey(username)) {
            users.get(username)[0] = newPassword;
            saveUsersToFile();
        }
    }

    public String[] getUserDetails(String username) {
        return users.getOrDefault(username, new String[]{"", "Unknown", "Unknown", "", ""});
    }

    public void updateUserNotes(String username, String notes) {
        if (users.containsKey(username)) {
            users.get(username)[3] = notes;
            saveUsersToFile();
        }
    }

    public void updateProfilePic(String username, String path) {
        if (users.containsKey(username)) {
            users.get(username)[4] = path;
            saveUsersToFile();
        }
    }
    // Check if an email is already registered to any account
    public boolean isEmailRegistered(String email) {
        for (String[] details : users.values()) {
            if (details[2].equalsIgnoreCase(email)) { 
                return true;
            }
        }
        return false;
    }

    // PERMANENTLY DELETE USER AND THEIR DATA
    public void deleteUserAccount(String username) {
        if (users.containsKey(username)) {
            // 1. Delete the user's specific inventory CSV file
            File inventoryFile = new File("inventory_" + username + ".csv");
            if (inventoryFile.exists()) {
                inventoryFile.delete();
            }
            
            // 2. Remove from the internal Map
            users.remove(username);
            
            // 3. Save the updated user list back to users_data.csv
            saveUsersToFile();
            logActivity("Account permanently deleted for user: " + username);
        }
    }

    // private void loadUsersFromFile() {
    //     try {
    //         File file = new File(USERS_FILE);
    //         if (!file.exists()) return;
    //         BufferedReader reader = new BufferedReader(new FileReader(file));
    //         String line;
    //         while ((line = reader.readLine()) != null) {
    //             String[] data = line.split(",");
    //             if (data.length >= 7) {
    //                 String notes = "";
    //                 try { notes = new String(Base64.getDecoder().decode(data[4])); } catch (Exception e) {}
    //                 users.put(data[0], new String[]{data[1], data[2], data[3], notes, data[5], data[6]});
    //             } else if (data.length >= 5) {
    //                 String notes = "";
    //                 try { notes = new String(Base64.getDecoder().decode(data[4])); } catch (Exception e) {}
    //                 users.put(data[0], new String[]{data[1], data[2], data[3], notes, ""});
    //             } else if (data.length >= 4) {
    //                 users.put(data[0], new String[]{data[1], data[2], data[3], "", ""});
    //             } else if (data.length == 2) { 
    //                 users.put(data[0], new String[]{data[1], "Unknown", "Unknown", "", ""});
    //             }
    //         }
    //         reader.close();
    //     } catch (IOException e) { e.printStackTrace(); }
    // }

// private void saveUsersToFile() {
//         try {
//             BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE));
//             for (Map.Entry<String, String[]> entry : users.entrySet()) {
//                 String[] d = entry.getValue();
//                 String notesB64 = Base64.getEncoder().encodeToString(d[3].getBytes());
                
//                 // Now writing 7 columns including the phone number (d[5])
//                 writer.write(entry.getKey() + "," + d[0] + "," + d[1] + "," + d[2] + "," + notesB64 + "," + d[4] + "," + d[5]);
//                 writer.newLine();
//             }
//             writer.close();
//         } catch (IOException e) { e.printStackTrace(); }
//     }

    private void loadUsersFromFile() {
        try {
            File file = new File(USERS_FILE);
            if (!file.exists()) return;
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                // Decrypt the line before reading the data
                line = decrypt(line); 
                
                String[] data = line.split(",");
                if (data.length >= 7) {
                    String notes = "";
                    try { notes = new String(Base64.getDecoder().decode(data[4])); } catch (Exception e) {}
                    users.put(data[0], new String[]{data[1], data[2], data[3], notes, data[5], data[6]});
                } else if (data.length >= 5) {
                    String notes = "";
                    try { notes = new String(Base64.getDecoder().decode(data[4])); } catch (Exception e) {}
                    users.put(data[0], new String[]{data[1], data[2], data[3], notes, ""});
                } else if (data.length >= 4) {
                    users.put(data[0], new String[]{data[1], data[2], data[3], "", ""});
                } else if (data.length == 2) { 
                    users.put(data[0], new String[]{data[1], "Unknown", "Unknown", "", ""});
                }
            }
            reader.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void saveUsersToFile() {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE));
            for (Map.Entry<String, String[]> entry : users.entrySet()) {
                String[] d = entry.getValue();
                String notesB64 = Base64.getEncoder().encodeToString(d[3].getBytes());
                
                // Combine the line first, then encrypt the entire line
                String plainLine = entry.getKey() + "," + d[0] + "," + d[1] + "," + d[2] + "," + notesB64 + "," + d[4] + "," + d[5];
                writer.write(encrypt(plainLine));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    public Map<String, Integer> getCategoryDistribution() {
        Map<String, Integer> dist = new HashMap<>();
        for (Product p : products) {
            dist.put(p.getCategory(), dist.getOrDefault(p.getCategory(), 0) + p.getQuantity());
        }
        return dist;
    }

    public void addProduct(Product product) { 
        products.add(product); 
        logActivity("Added new product: " + product.getName() + " (Qty: " + product.getQuantity() + ")");
    }

    public void removeProduct(int index) {
        if (index >= 0 && index < products.size()) { 
            String name = products.get(index).getName();
            products.remove(index); 
            logActivity("Deleted product: " + name);
        }
    }

    public Product getProduct(int index) { return products.get(index); }
    public List<Product> getAllProducts() { return products; }
    public List<String> getLogs() { return activityLogs; }

    public double calculateTotalValue() {
        double total = 0;
        for (Product p : products) { total += (p.getPrice() * p.getQuantity()); }
        return total;
    }

    public int getLowStockCount() {
        int count = 0;
        for (Product p : products) { if (p.getQuantity() < 20) count++; }
        return count;
    }

    public void logActivity(String action) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        activityLogs.add("[" + dtf.format(now) + "] " + action);
    }

public List<Product> searchProducts(String keyword) {
        List<Product> results = new ArrayList<>();
        String lowerKeyword = keyword.toLowerCase();
        for (Product p : products) {
            // Now searches ID, Name, Category, and Supplier Name!
            if (p.getName().toLowerCase().contains(lowerKeyword) || 
                p.getId().toLowerCase().contains(lowerKeyword) ||
                p.getCategory().toLowerCase().contains(lowerKeyword) ||
                p.getSupplierName().toLowerCase().contains(lowerKeyword)) {
                results.add(p);
            }
        }
        return results;
    }

//     public void saveToFile() throws IOException {
//         BufferedWriter writer = new BufferedWriter(new FileWriter(currentUserFile));
//         for (Product p : products) {
//             writer.write(p.getId() + "," + p.getName() + "," + p.getCategory() + "," 
//                          + p.getQuantity() + "," + p.getPrice() + "," + p.getTargetDate() + "," 
//                          + p.getSupplierStatus() + "," + p.getSupplierName() + "," + p.getLeadTimeDays());
//             writer.newLine();
//         }
//         writer.close();
//         logActivity("Database saved successfully.");
//     }

// public void loadFromFile() throws IOException {
//         File file = new File(currentUserFile);
//         if (!file.exists()) return;
        
//         products.clear();
//         BufferedReader reader = new BufferedReader(new FileReader(file));
//         String line;
//         while ((line = reader.readLine()) != null) {
//             String[] data = line.split(",");
//             // This is the correct logic for Products, not Users!
//             if (data.length >= 9) { 
//                 products.add(new Product(data[0], data[1], data[2], 
//                     Integer.parseInt(data[3]), Double.parseDouble(data[4]), 
//                     data[5], data[6], data[7], Integer.parseInt(data[8])));
//             }
//         }
//         reader.close();
//         logActivity("Database loaded successfully.");
//     }

    public void saveToFile() throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(currentUserFile));
        for (Product p : products) {
            // Combine line first, then encrypt
            String plainLine = p.getId() + "," + p.getName() + "," + p.getCategory() + "," 
                         + p.getQuantity() + "," + p.getPrice() + "," + p.getTargetDate() + "," 
                         + p.getSupplierStatus() + "," + p.getSupplierName() + "," + p.getLeadTimeDays();
            writer.write(encrypt(plainLine));
            writer.newLine();
        }
        writer.close();
        logActivity("Database saved successfully.");
    }

    public void loadFromFile() throws IOException {
        File file = new File(currentUserFile);
        if (!file.exists()) return;
        
        products.clear();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null) {
            // Decrypt the line before splitting
            line = decrypt(line); 
            
            String[] data = line.split(",");
            if (data.length >= 9) { 
                products.add(new Product(data[0], data[1], data[2], 
                    Integer.parseInt(data[3]), Double.parseDouble(data[4]), 
                    data[5], data[6], data[7], Integer.parseInt(data[8])));
            }
        }
        reader.close();
        logActivity("Database loaded successfully.");
    }
}