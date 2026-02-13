package model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Ticket {

    // --- Attributes ---
    private String ticketID;      // Format: T-PLATE-TIMESTAMP
    private String vehiclePlate;
    private String spotID;        // for example: "F1-R1-S5"
    private String vehicleType;   // for example: "Car", "SUV"
    private LocalDateTime entryTime;

    // --- Private Constructor (Encapsulation) ---
    private Ticket(String ticketID, String vehiclePlate, String spotID, String vehicleType, LocalDateTime entryTime) {
        this.ticketID = ticketID;
        this.vehiclePlate = vehiclePlate;
        this.spotID = spotID;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
    }

    // --- Factory Method: Create NEW Ticket ---
    // Usage: Ticket t = Ticket.create_new("ABC1234", "F1-R1-S1", "Car");
    public static Ticket create_new(String vehiclePlate, String spotID, String vehicleType) {
        LocalDateTime now = LocalDateTime.now();
        
    
        // We use a short timestamp (MMddHHmm) to keep ID readable
        String timeStamp = now.format(DateTimeFormatter.ofPattern("MMddHHmm"));
        String newID = "T-" + vehiclePlate + "-" + timeStamp;

        return new Ticket(newID, vehiclePlate, spotID, vehicleType, now);
    }

    // --- Factory Method: Load EXISTING Ticket (From Database) ---
    public static Ticket load_existing(String ticketID, String vehiclePlate, String spotID, String vehicleType, String dateStr) {
        LocalDateTime date = LocalDateTime.parse(dateStr);
        return new Ticket(ticketID, vehiclePlate, spotID, vehicleType, date);
    }

    // --- Getters ---
    public String getTicketID() { return ticketID; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getSpotID() { return spotID; }
    public String getVehicleType() { return vehicleType; }
    public LocalDateTime getEntryTime() { return entryTime; }

    @Override
    public String toString() {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM HH:mm");
        return String.format("[TICKET] ID: %s | Plate: %s | Spot: %s | Entered: %s", 
            ticketID, vehiclePlate, spotID, entryTime.format(fmt));
    }
}