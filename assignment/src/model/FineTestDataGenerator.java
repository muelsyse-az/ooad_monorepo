package model;

import java.time.LocalDateTime;
import java.util.Random;

public class FineTestDataGenerator {

    public static void main(String[] args) {
        System.out.println("Starting data generation..."); 
        generateTestData();
        System.out.println("Data generation complete! You can now refresh your UI."); 
    }

    public static void generateTestData() {
        String[] plates = {"WAA1111", "KUL3333", "JDT2222", "BEE5555", "PKR9999"}; 
        String[] reasons = {"Illegal Parking", "Overstayed", "Obstruction", "No Permit"}; 
        Random rand = new Random();

        for (int i = 0; i < 10; i++) {
            String plate = plates[rand.nextInt(plates.length)];
            String reason = reasons[rand.nextInt(reasons.length)];
            double amount = 50.0 + (rand.nextInt(5) * 25.0); // Randomly 50, 75, 100, 125, or 150 [cite: 2026-02-13]
            
            // Create a new Fine object (the constructor handles ID generation and current time) [cite: 2026-01-15, 2026-02-13]
            Fine fakeFine = new Fine(
                "F-" + (1000 + i),         // ID must be a String
                plate, 
                amount, 
                reason, 
                "Standard", 
                rand.nextBoolean(), 
                LocalDateTime.now(), 
                (LocalDateTime) null,      // Cast null to LocalDateTime
                (String) null              // Cast null to String
            );
            // Randomly set some as already PAID to test your status filters [cite: 2026-02-13, 2026-02-14]
            if (rand.nextBoolean()) {
                fakeFine.pay("RandomGenerated");;
            }

            // Save to database using your existing Manager logic [cite: 2026-02-13]
            FineManager.save_fine(fakeFine);
        }
    }
}