package Models;

import java.util.ArrayList;
import java.util.List;

public class FineTest {
    public static void main(String[] args) {
        
        System.out.println("--- SYSTEM START: Initializing Database ---");
        DatabaseManager.initialize_database();
        
        List<Fine> fineDatabase = new ArrayList<>();

        // 1. Create Fines
        Fine fine1 = new Fine("WAA1234", 50.00, "Overstayed > 24 hours", "Fixed");
        Fine fine2 = new Fine("JDT999", 50.00, "Parked in Reserved Spot", "Fixed");

        fineDatabase.add(fine1);
        fineDatabase.add(fine2);

        System.out.println("\n--- SAVING NEW FINES TO SQLITE ---");
        DatabaseManager.save_fine(fine1);
        DatabaseManager.save_fine(fine2);

        // 2. Admin View
        System.out.println("\n--- ADMIN VIEW: All Outstanding Fines ---");
        for (Fine f : fineDatabase) {
            System.out.println(f.toString());
        }

        // 3. Customer Pays
        System.out.println("\n--- EXIT PANEL: Customer WAA1234 is paying their fine... ---");
        for (Fine f : fineDatabase) {
            if (f.getVehiclePlate().equals("WAA1234") && !f.isPaid()) {
                f.pay("Credit Card"); 
                System.out.println("Payment successful for " + f.getFineID());
                
                // BOOM! We save it again to update the row in SQLite!
                DatabaseManager.save_fine(f); 
            }
        }

        // 4. Final Admin View
        System.out.println("\n--- ADMIN VIEW: Updated Database ---");
        for (Fine f : fineDatabase) {
            System.out.println(f.toString());
        }

        //5. Generate Fine Revenue
        System.out.println("\n--- ADMIN VIEW: Fine Revenue Report ---");
        DatabaseManager.generate_fine_revenue_report("2026-02-11");
    }
}