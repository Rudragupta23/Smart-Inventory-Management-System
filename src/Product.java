public class Product {
    private String id;
    private String name;
    private String category;
    private int quantity;
    private double price;
    private String targetDate; 
    private String supplierStatus;
    private String supplierName;
    private int leadTimeDays; 

    public Product(String id, String name, String category, int quantity, double price, String targetDate, String supplierStatus, String supplierName, int leadTimeDays) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.price = price;
        this.targetDate = targetDate;
        this.supplierStatus = supplierStatus;
        this.supplierName = supplierName;
        this.leadTimeDays = leadTimeDays;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public int getQuantity() { return quantity; }
    public double getPrice() { return price; }
    public String getTargetDate() { return targetDate; }
    public String getSupplierStatus() { return supplierStatus; }
    public String getSupplierName() { return supplierName; }
    public int getLeadTimeDays() { return leadTimeDays; } 

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setPrice(double price) { this.price = price; }
    public void setSupplierStatus(String supplierStatus) { this.supplierStatus = supplierStatus; }
}