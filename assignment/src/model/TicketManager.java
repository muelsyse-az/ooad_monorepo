package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class TicketManager {

    // --- RATES ---
    private static final double RATE_COMPACT = 2.00;
    private static final double RATE_REGULAR = 5.00;
    private static final double RATE_HANDICAPPED = 2.00; 
    private static final double RATE_RESERVED = 10.00;

    // =======================================================================================
    // 1. ENTRY AND EXIT LOGIC
    // =======================================================================================

    // --- 1: Issue a Ticket (Entry) ---
    public static Ticket issue_ticket(String plate, String spotID, String type) {
        
        // 1. Check if they are barred/have fines!
        if (FineManager.is_vehicle_barred(plate)) {
            System.out.println("   [ALERT] Entry Denied: Vehicle " + plate + " has outstanding fines!");
            // return null
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
        DatabaseManager.update_spot_status(spotID, true); // Mark spot as occupied
        System.out.println("   [SUCCESS] Ticket generated: " + newTicket.getTicketID());
        return newTicket;
    }

    // --- 2: Calculate Duration (Time) ---
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

    // --- 3: Calculate Total Fee ---
    public static double calculate_total_fee(String plate) {
        
        // 1. Get the Ticket Details
        Ticket t = DatabaseManager.get_active_ticket(plate);
        if (t == null) {
            System.out.println("   [ERROR] Cannot calculate fee. No ticket found.");
            return 0.0;
        }

    // 2. Calculate Duration
    long hours = calculate_duration(plate);
    double hourlyRate = get_rate_for_type(t.getVehicleType()); // helper method to get rate
    double parkingFee = hours * hourlyRate;

    // 5. Check for Unpaid Fines
    Fine unpaidFine = FineManager.get_fine(plate, false);
    double fineAmount = (unpaidFine != null) ? unpaidFine.getAmount() : 0.0;
    
    // 6. Return Total
    return parkingFee + fineAmount;
    }

    // --- FACADE METHOD 4: Process Payment & Exit (The "Master" Function) ---
    // CALL THIS WHEN THE USER CLICKS "PAY"
    public static void process_successful_payment(String plate, String paymentMethod) {
        
        // 1. Pay any outstanding fines first
        Fine unpaidFine = FineManager.get_fine(plate, false);
        if (unpaidFine != null) {
            System.out.println("   [PAYMENT] Clearing outstanding fine of RM " + unpaidFine.getAmount());
            FineManager.process_payment(plate, paymentMethod); 
        }

        // 2. Close the parking ticket
        close_ticket(plate);
        System.out.println("   [COMPLETE] Transaction finished. Have a nice day!");
    }

    // --- internal helper: Closwe Ticket & Log Exit ---
    public static void close_ticket(String plate) {
        Ticket t = DatabaseManager.get_active_ticket(plate);

        if (t == null) {
            System.out.println("   [ERROR] No active ticket found for " + plate);
            return;
        }

    
        // exit time is NOW
        LocalDateTime exit = LocalDateTime.now();

        // save history log (plate, type, entry, exit)
        VehicleLog log = new VehicleLog(
                t.getTicketID(),
                t.getVehiclePlate(),
                t.getSpotID(),
                t.getVehicleType(),
                t.getEntryTime(),
                exit
        );
        DatabaseManager.save_vehicle_log(log);

        // keep your existing behavior: remove active ticket after exit
        boolean success = DatabaseManager.delete_ticket(plate);

        if (success) {
            System.out.println("   [INFO] Gate Open + Spot " + t.getSpotID() + ". is now free.");
        } else {
            System.out.println("   [ERROR] Could not close ticket. Database error.");
        }
    }

    // Returns a list of ALL vehicles currently inside the parking lot
    public static List<Ticket> get_all_active_tickets() {
        return DatabaseManager.get_all_tickets();
    }

    // Returns Total count of cars currently parked
    public static int get_current_parked_count() {
        return get_all_active_tickets().size();
    }

    // Filter tickets by type
    public static List<Ticket> filter_tickets_by_type(String type) {
        return get_all_active_tickets().stream()
                .filter(t -> t.getVehicleType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // Search for specific ticket by Plate or Ticket ID
    public static Ticket search_ticket(String query) {
        return DatabaseManager.find_ticket_by_id_or_plate(query);
    }

    // --- Helper for Rates ---
    private static double get_rate_for_type(String type) {
        type = type.toLowerCase();
        if (type.contains("motorcycle") || type.contains("compact")) return RATE_COMPACT;
        if (type.contains("suv") || type.contains("truck") || type.contains("car")) return RATE_REGULAR;
        if (type.contains("handicapped")) return RATE_HANDICAPPED;
        if (type.contains("vip") || type.contains("reserved")) return RATE_RESERVED;
        return RATE_REGULAR; // Default rate
    }


}