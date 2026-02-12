package model;

public class FineTest {
    public static void main(String[] args) {

        DatabaseManager.clear_fines_table(); // Start with a fresh slate for testing
        // --- 0. SYSTEM INITIALIZATION ---
        System.out.println("--- SYSTEM START: FULL FEATURE TEST ---");
        DatabaseManager.initialize_fine_table();
        

        // --- 1. TESTING VARIOUS FINE SCHEMES ---
        System.out.println("\n--- [TEST 1] Testing Calculation Schemes ---");
        
        // Scheme A: Fixed (Should be 50.00)
        FineManager.issue_fine("WAA1111", "Illegal Parking", "Fixed", 0);
        
        // Scheme B: Progressive (30 hours -> Tier 2 -> RM 150.00)
        FineManager.issue_fine("JDT2222", "Overstayed", "Progressive", 30.0);
        
        // Scheme C: Hourly (4 hours -> RM 80.00)
        FineManager.issue_fine("KUL3333", "Overstayed", "Hourly", 4.0);

        // --- 2. TESTING "ONE FINE PER VEHICLE" RULE ---
        System.out.println("\n--- [TEST 2] Testing One-Fine Constraint ---");
        // Attempting to issue another fine to JDT2222 while they still owe RM 150
        FineManager.issue_fine("JDT2222", "Speeding", "Fixed", 0);

        // --- 3. TESTING GATE ACCESS & PAYMENT ---
        System.out.println("\n--- [TEST 3] Testing Gate Check & Payment ---");
        
        // Check KUL3333 (Should be blocked)
        System.out.print("Exit Gate for KUL3333: ");
        FineManager.is_vehicle_barred("KUL3333");

        // Process Payment for KUL3333
        FineManager.process_payment("KUL3333", "Credit Card");

        // Check KUL3333 again (Should be clear)
        System.out.print("Exit Gate for KUL3333 after payment: ");
        FineManager.is_vehicle_barred("KUL3333");

        // --- 4. TESTING REVOCATION (ADMIN UNDO) ---
        System.out.println("\n--- [TEST 4] Testing Admin Revocation ---");
        
        // Let's issue a fine by mistake
        FineManager.issue_fine("MISTAKE_X", "Wrongful Fine", "Fixed", 0);
        
        // Find the ID to revoke it
        Fine target = DatabaseManager.get_fine("MISTAKE_X", false);
        if (target != null) {
            FineManager.revoke_fine(target.getFineID());
        }

        // --- 5. TESTING ADMIN LIST VIEWS & REPORTS ---
        System.out.println("\n--- [TEST 5] Final Admin Audit ---");
        
        DatabaseManager.view_all_unpaid_fines();

        DatabaseManager.view_vehicle_fine_history("JDT2222");

        // Check history for KUL3333 (Should show their paid fine)
        DatabaseManager.view_vehicle_fine_history("KUL3333");

        // Final Revenue Report
        DatabaseManager.generate_fine_revenue_report("2026-02-12");
        DatabaseManager.view_all_fines();

        System.out.println("\n--- ALL TESTS COMPLETE ---");
    }
}