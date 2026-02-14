package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TicketManager {

    // --- RATES (From Assignment PDF) ---
    private static final double RATE_COMPACT = 2.00;
    private static final double RATE_REGULAR = 5.00;
    private static final double RATE_HANDICAPPED = 2.00; 
    private static final double RATE_RESERVED = 10.00;

    // --- FACADE METHOD 1: Issue a Ticket (Entry) ---
    public static Ticket issue_ticket(String plate, String spotID, String type) {
        
        // 1. [NEW] Check if they are barred/have fines!
        if (FineManager.is_vehicle_barred(plate)) {
            System.out.println("   [ALERT] Entry Denied: Vehicle " + plate + " has outstanding fines!");
            // You could return null here to block them. 
            // For now, we print the alert but let them in (as per requirements).
        }

        // 2. Check if vehicle is already inside
        Ticket existing = DatabaseManager.get_active_ticket(plate);
        if (existing != null) {
            System.out.println("   [ERROR] Vehicle " + plate + " is already inside.");
            return existing;
        }

        // 3. Create and Save
        Ticket newTicket = Ticket.create_new(plate, spotID, type);
        DatabaseManager.save_ticket(newTicket);
        ParkingSpotManager.occupy_spot(spotID);
        System.out.println("   [SUCCESS] Ticket generated: " + newTicket.getTicketID());
        return newTicket;
    }

    // --- FACADE METHOD 2: Calculate Duration (Time) ---
    public static long calculate_duration(String plate) {
        Ticket t = DatabaseManager.get_active_ticket(plate);
        if (t == null) return 0;

        LocalDateTime entry = t.getEntryTime();
        LocalDateTime exit = LocalDateTime.now();

        // Rule: Rounded up to the nearest hour (Ceiling rounding)
        long minutes = ChronoUnit.MINUTES.between(entry, exit);
        long hours = (long) Math.ceil(minutes / 60.0);
        
        if (hours <= 0) hours = 1; // Minimum 1 hour charge
        return hours;
    }

    // --- FACADE METHOD 3: Calculate Total Fee (Money) ---
    public static double calculate_total_fee(String plate) {
        
        // 1. Get the Ticket Details
        Ticket t = DatabaseManager.get_active_ticket(plate);
        if (t == null) {
            System.out.println("   [ERROR] Cannot calculate fee. No ticket found.");
            return 0.0;
        }

        // 2. Calculate Duration
        long hours = calculate_duration(plate);

        // 3. Determine Rate based on Vehicle Type
        double hourlyRate = 0.0;
        String type = t.getVehicleType().toLowerCase();

        if (type.contains("motorcycle") || type.contains("compact")) {
            hourlyRate = RATE_COMPACT;
        } else if (type.contains("suv") || type.contains("truck") || type.contains("car")) {
            hourlyRate = RATE_REGULAR;
        } else if (type.contains("vip") || type.contains("reserved")) {
            hourlyRate = RATE_RESERVED;
        } else {
            hourlyRate = RATE_REGULAR;
        }

        // 4. Calculate Base Parking Fee
        double parkingFee = hours * hourlyRate;

        // 5. Check for Unpaid Fines
        Fine unpaidFine = DatabaseManager.get_fine(plate, false);
        double fineAmount = (unpaidFine != null) ? unpaidFine.getAmount() : 0.0;
        
        // 6. Return Total
        return parkingFee + fineAmount;
    }

    // --- FACADE METHOD 4: Process Payment & Exit (The "Master" Function) ---
    // CALL THIS WHEN THE USER CLICKS "PAY"
    public static void process_successful_payment(String plate, String paymentMethod) {
        
        // 1. Pay any outstanding fines first
        Fine unpaidFine = DatabaseManager.get_fine(plate, false);
        if (unpaidFine != null) {
            System.out.println("   [PAYMENT] Clearing outstanding fine of RM " + unpaidFine.getAmount());
            FineManager.process_payment(plate, paymentMethod); 
        }

        // 2. Close the parking ticket
        close_ticket(plate);

        System.out.println("   [COMPLETE] Transaction finished. Have a nice day!");
    }
    
    // --- Helper Method to Delete Ticket ---
    public static void close_ticket(String plate) {
        Ticket t = DatabaseManager.get_active_ticket(plate);
        if(t != null) {
            ParkingSpotManager.free_spot(t.getSpotID());
        }
        if (DatabaseManager.delete_ticket(plate)) {
            System.out.println("   [INFO] Ticket closed. Gate Open.");
        }
    }
}