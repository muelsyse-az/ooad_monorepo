package model;

public class TicketTest {
    public static void main(String[] args) {
        System.out.println("=== SYSTEM STARTUP ===");
        
        // 1. Initialize Tables
        DatabaseManager.initialize_ticket_table(); 

        System.out.println("\n=== TEST 1: VEHICLE ENTRY ===");
        // Simulate a user entering: Plate JVA123, Spot F1-R1-S1, Type Car
        Ticket myTicket = TicketManager.issue_ticket("JVA123", "F1-R1-S1", "Car");
        
        // Display what the GUI would show
        System.out.println("GUI DISPLAY: " + myTicket.toString());

        System.out.println("\n=== TEST 2: DUPLICATE ENTRY CHECK ===");
        // Try to enter again with the same car
        TicketManager.issue_ticket("JVA123", "F2-R5-S5", "Car");

        System.out.println("\n=== TEST 3: RETRIEVE FROM DB ===");
        // Simulate checking the database directly
        Ticket dbTicket = DatabaseManager.get_active_ticket("JVA123");
        if (dbTicket != null) {
            System.out.println("DB Record Found: " + dbTicket.getTicketID());
        }

        System.out.println("\n=== TEST 4: EXIT CALCULATION ===");
        // Calculate hours parked
        long hours = TicketManager.calculate_duration("JVA123");
        System.out.println("Duration calculated: " + hours + " Hours");
        
        System.out.println("\n=== TEST 5: EXIT & DELETE ===");
        TicketManager.close_ticket("JVA123");
        
        // Verify deletion
        Ticket check = DatabaseManager.get_active_ticket("JVA123");
        if (check == null) {
            System.out.println("Verification: Ticket successfully removed from Active list.");
        }
    }
}