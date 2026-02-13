package model;

public class FineManager {

    
    public static double calculate_fine_amount(String schemeType, double overstayHours) {
        double amount = 0.0;

        switch (schemeType) {
            case "Fixed":
                // Option A: Flat RM 50
                amount = 50.00;
                break;

            case "Progressive":
                // Option B: Tiered fines
                if (overstayHours <= 24) {
                    amount = 50.00;
                } else if (overstayHours <= 48) {
                    amount = 50.00 + 100.00; // RM 150
                } else if (overstayHours <= 72) {
                    amount = 50.00 + 100.00 + 150.00; // RM 300
                } else {
                    amount = 50.00 + 100.00 + 150.00 + 200.00; // RM 500
                }
                break;

            case "Hourly":
                // Option C: RM 20 per hour (Rounded up to nearest hour usually, but we'll use exact for now)
                // If you want to charge for partial hours (e.g. 1.5 hours = RM 30), keep as is.
                // If you want to charge full hours (e.g. 1.1 hours = 2 hours = RM 40), use Math.ceil(overstayHours)
                amount = Math.ceil(overstayHours) * 20.00;
                break;

            default:
                System.out.println("   [WARNING] Unknown scheme. Defaulting to Fixed.");
                amount = 50.00;
        }
        return amount;
    }

    // --- 1. ISSUE FINE (Updated with Math) ---
    public static void issue_fine(String vehiclePlate, String reason, String schemeType, double overstayHours) {
        // RULE 1: Check if car already has an UNPAID fine
        Fine existingFine = DatabaseManager.get_fine(vehiclePlate, false);

        if (existingFine != null) {
            // RULE: "If customer leaves without paying... next exit shows unpaid fine"
            // Since it already exists in DB, we don't overwrite it. We just remind them.
            System.out.println("   [ERROR] Vehicle " + vehiclePlate + " already has outstanding fine: " + existingFine.getFineID());
            System.out.println("   Current Amount Due: RM " + existingFine.getAmount());
            return;
        }

        // RULE 2: Calculate Amount based on the chosen scheme
        double finalAmount = calculate_fine_amount(schemeType, overstayHours);

        // Create the object
        Fine newFine = Fine.create_new(vehiclePlate, finalAmount, reason, schemeType);

        // Save it permanently
        DatabaseManager.save_fine(newFine);
        System.out.println("   [SUCCESS] New fine issued to " + vehiclePlate);
        System.out.println("   Scheme: " + schemeType + " | Hours: " + overstayHours + " | Total: RM " + finalAmount);
    }

    // --- 2. PROCESS PAYMENT (Unchanged) ---
    public static void process_payment(String vehiclePlate, String paymentMethod) {
        Fine fineToPay = DatabaseManager.get_fine(vehiclePlate, false);

        if (fineToPay == null) {
            System.out.println("   [INFO] No unpaid fines found for " + vehiclePlate);
            return;
        }

        fineToPay.pay(paymentMethod);
        DatabaseManager.save_fine(fineToPay);
        System.out.println("   [SUCCESS] Payment recorded. Fine " + fineToPay.getFineID() + " is CLEARED.");
    }

    // --- 3. GATE CHECK (Unchanged) ---
    public static boolean is_vehicle_barred(String vehiclePlate) {
        Fine unpaidFine = DatabaseManager.get_fine(vehiclePlate, false);
        
        if (unpaidFine != null) {
            System.out.println("   [ALERT] GATE BLOCKED! " + vehiclePlate + " owes RM " + unpaidFine.getAmount());
            // This satisfies the requirement: "Next exit will show the unpaid fine"
            return true; 
        }
        return false;
    }

    // --- 4. ADMIN: REVOKE FINE (Undo Mistake) ---
    public static void revoke_fine(String fineID) {
        boolean success = DatabaseManager.delete_fine(fineID);

        if (success) {
            System.out.println("   [SUCCESS] Fine " + fineID + " has been permanently REVOKED.");
        } else {
            System.out.println("   [ERROR] Could not revoke fine " + fineID + ". ID not found.");
        }
    }
}