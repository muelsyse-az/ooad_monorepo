package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TicketManager {

    // --- FACADE METHOD 1: Issue a Ticket (Entry) ---
    // Returns the Ticket object to be displayed by the UI
    public static Ticket issue_ticket(String plate, String spotID, String type) {
        
        // 1. Check if vehicle is already inside? (Optional validation)
        Ticket existing = DatabaseManager.get_active_ticket(plate);
        if (existing != null) {
            System.out.println("   [ERROR] Vehicle " + plate + " is already inside with Ticket " + existing.getTicketID());
            return existing; // Return existing ticket instead of creating a duplicate
        }

        // 2. Create the Model
        Ticket newTicket = Ticket.create_new(plate, spotID, type);

        // 3. Save to Database
        DatabaseManager.save_ticket(newTicket);
        
        System.out.println("   [SUCCESS] Ticket generated: " + newTicket.getTicketID());
        return newTicket;
    }

    // --- FACADE METHOD 2: Process Exit (Calculation) ---
    // Returns the duration in hours (rounded up)
    public static long calculate_duration(String plate) {
        Ticket t = DatabaseManager.get_active_ticket(plate);
        
        if (t == null) {
            System.out.println("   [ERROR] No active ticket found for " + plate);
            return 0;
        }

        LocalDateTime entry = t.getEntryTime();
        LocalDateTime exit = LocalDateTime.now();

        // Calculate minutes, then convert to hours (Ceiling Rounding)
        long minutes = ChronoUnit.MINUTES.between(entry, exit);
        long hours = (long) Math.ceil(minutes / 60.0);
        
        // Minimum parking is usually 1 hour
        if (hours == 0) hours = 1; 

        return hours;
    }
    
    // --- FACADE METHOD 3: Remove Ticket (After Payment) ---
    public static void close_ticket(String plate) {
        boolean success = DatabaseManager.delete_ticket(plate);
        if (success) {
            System.out.println("   [INFO] Ticket closed for " + plate + ". Gate opening...");
        } else {
            System.out.println("   [ERROR] Could not close ticket. Database error.");
        }
    }
}