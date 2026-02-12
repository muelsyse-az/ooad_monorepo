package Models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Fine {
    
    // Static counter to generate unique IDs automatically
    private static int fineCounter = 1000;

    // --- Attributes (Private for Encapsulation) ---
    private String fineID;
    private String fineSchemeType; // e.g., "Fixed", "Progressive", "Hourly"
    private String vehiclePlate;   // Linked directly to the license plate
    private double amount;
    private String reason;         // e.g., "Overstayed by 2 hours"
    private LocalDateTime issueDate;
    
    // Status and Payment Tracking
    private boolean isPaid;
    private LocalDateTime paymentDate;
    private String paymentMethod;  // e.g., "Cash", "Card"

    // --- Constructor ---
    // This is called when you create a new Fine using "new Fine(...)"
    private Fine(String fineID, String vehiclePlate, double amount, String reason, String fineSchemeType, boolean isPaid, LocalDateTime issueDate) {
        this.fineID = fineID;
        this.vehiclePlate = vehiclePlate;
        this.amount = amount;
        this.reason = reason;
        this.fineSchemeType = fineSchemeType;
        this.isPaid = isPaid;
        this.issueDate = issueDate;
    }

    // 2. Factory Method for NEW Fines (Clear Name!)
    public static Fine create_new(String vehiclePlate, double amount, String reason, String fineSchemeType) {
        // Generates ID and Date automatically
        fineCounter++;
        String newID = "F-" + fineCounter;
        LocalDateTime now = LocalDateTime.now();
        
        return new Fine(newID, vehiclePlate, amount, reason, fineSchemeType, false, now);
    }

    // 3. Factory Method for LOADING Fines (Clear Name!)
    public static Fine load_existing(String fineID, String vehiclePlate, double amount, String reason, String fineSchemeType, boolean isPaid, String dateStr) {
        
        // VALIDATION CHECK: Data Integrity
        if (dateStr == null || dateStr.trim().isEmpty()) {
            //Stop everything (Good for catching bugs during development)
            throw new IllegalArgumentException("CRITICAL DATA ERROR: Fine " + fineID + " has no issue date in the database!");
        }

        LocalDateTime date = LocalDateTime.parse(dateStr);
        return new Fine(fineID, vehiclePlate, amount, reason, fineSchemeType, isPaid, date);
    }

    // --- Core Operations ---
    
    // Called when the customer pays the fine at the exit
    public void pay(String method) {
        this.isPaid = true;
        this.paymentMethod = method;
        this.paymentDate = LocalDateTime.now(); // Stamps the exact time of payment
    }

    // --- Getters (Accessors to read the private data) ---
    
    public String getFineID() {
        return fineID;
    }

    public String getVehiclePlate() {
        return vehiclePlate;
    }

    public double getAmount() {
        return amount;
    }

    public boolean isPaid() {
        return isPaid;
    }

    public String getReason() {
        return reason;
    }
    
    public String getFineSchemeType() {
        return fineSchemeType;
    }

    public LocalDateTime getIssueDate() {
        return issueDate;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
        String formattedDate = issueDate.format(formatter);
        
        String statusText = isPaid ? "PAID" : "UNPAID";
        
        return String.format("[%s] Plate: %s | Amount: RM %.2f | Reason: %s | Date: %s | Status: %s", 
                fineID, vehiclePlate, amount, reason, formattedDate, statusText);
    }
}
