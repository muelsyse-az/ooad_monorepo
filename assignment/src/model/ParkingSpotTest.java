package model;

public class ParkingSpotTest {
    public static void main(String[] args) {
        System.out.println("--- TESTING PARKING SPOT RULES ---");

        testRule("MOTORCYCLE", "COMPACT", true);
        testRule("MOTORCYCLE", "REGULAR", false); // Should Fail [cite: 377]

        testRule("CAR", "COMPACT", true);
        testRule("CAR", "REGULAR", true);         // [cite: 378]

        testRule("SUV", "COMPACT", false);        // Should Fail
        testRule("SUV", "REGULAR", true);         // [cite: 379]

        System.out.println("\n--- TESTING DATABASE INIT ---");
        DatabaseManager.initialize_spot_table();
        ParkingSpotManager.reset_and_initialize_lot(); // Creates the 5 floors
    }

    private static void testRule(String v, String s, boolean expected) {
        boolean result = ParkingSpotManager.can_park(v, s);
        String status = (result == expected) ? "PASS" : "FAIL";
        System.out.printf("[%s] Vehicle: %s -> Spot: %s = %b\n", status, v, s, result);
    }
}