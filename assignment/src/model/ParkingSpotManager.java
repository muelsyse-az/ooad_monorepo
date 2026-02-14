package model;

import java.util.ArrayList;
import java.util.List;

public class ParkingSpotManager {

    // Initialize the Parking Lot Structure (5 Floors) [cite: 360]
    // Call this ONCE when the app starts (or via Admin panel)
    public static void reset_and_initialize_lot() {
        // Clear old data (Optional, for fresh start)
        // DatabaseManager.clear_table("spots");

        System.out.println("Initializing Parking Lot Structure...");

        // Configuration: 5 Floors, 10 Rows, 5 Spots per Row
        for (int f = 1; f <= 5; f++) {
            for (int r = 1; r <= 10; r++) {
                for (int s = 1; s <= 5; s++) {

                    // Logic to distribute spot types
                    ParkingSpot.SpotType type;
                    if (f == 1) type = ParkingSpot.SpotType.HANDICAPPED; // Floor 1 is accessible
                    else if (f == 5) type = ParkingSpot.SpotType.RESERVED; // Top floor VIP
                    else if (s <= 2) type = ParkingSpot.SpotType.COMPACT; // Small spots
                    else type = ParkingSpot.SpotType.REGULAR;

                    String id = String.format("F%d-R%d-S%d", f, r, s);
                    ParkingSpot spot = new ParkingSpot(id, f, r, type, false, null);

                    DatabaseManager.save_spot(spot);
                }
            }
        }
        System.out.println("Parking Lot Initialized Successfully.");
    }

    // The Logic Engine: Can this vehicle park here?
    public static boolean can_park(String vehicleType, String spotType) {
        vehicleType = vehicleType.toUpperCase();
        spotType = spotType.toUpperCase();

        // 1. Motorcycle: Compact ONLY
        if (vehicleType.equals("MOTORCYCLE")) {
            return spotType.equals("COMPACT");
        }

        // 2. Car: Compact OR Regular
        if (vehicleType.equals("CAR")) {
            return spotType.equals("COMPACT") || spotType.equals("REGULAR");
        }

        // 3. SUV/Truck: Regular ONLY
        if (vehicleType.equals("SUV") || vehicleType.equals("TRUCK")) {
            return spotType.equals("REGULAR");
        }

        // 4. Handicapped Vehicle: ANY Spot (Priority logic handled in UI)
        if (vehicleType.equals("HANDICAPPED")) {
            return true;
        }

        return false; // Unknown type
    }

    // Facade: Occupy a spot
    public static void occupy_spot(String spotID) {
        DatabaseManager.update_spot_status(spotID, true);
    }

    // Facade: Free a spot
    public static void free_spot(String spotID) {
        DatabaseManager.update_spot_status(spotID, false);
    }
}