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
    public Fine(String vehiclePlate, double amount, String reason, String fineSchemeType) {
        this.fineID = generateFineID();
        this.vehiclePlate = vehiclePlate;
        this.amount = amount;
        this.reason = reason;
        this.fineSchemeType = fineSchemeType;
        
        this.issueDate = LocalDateTime.now(); // Automatically captures current date/time
        this.isPaid = false; // By default, a new fine is unpaid
    }

    // --- Private Helper Method ---
    private String generateFineID() {
        fineCounter++;
        return "F-" + fineCounter;
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
