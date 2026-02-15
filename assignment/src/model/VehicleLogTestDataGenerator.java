package model; // Change to 'package model;' if you prefer

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class VehicleLogTestDataGenerator {

    // Config: How many logs to generate?
    private static final int NUM_LOGS = 50; 
    
    private static final String[] TYPES = {"Car", "Motorcycle", "Van", "Truck"};
    private static final String[] PREFIXES = {"WAA", "JDT", "PEN", "BAM", "KUL", "SGR"};
    
    public static void main(String[] args) {
        System.out.println("Generating " + NUM_LOGS + " dummy vehicle logs...");
        generateLogs();
    }

    public static void generateLogs() {
        String sql = "INSERT INTO vehicle_logs(ticketID, vehiclePlate, spotID, vehicleType, entryTime, exitTime) "
                   + "VALUES(?,?,?,?,?,?)";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            Random rand = new Random();
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

            for (int i = 0; i < NUM_LOGS; i++) {
                // 1. Random Vehicle Details
                String plate = PREFIXES[rand.nextInt(PREFIXES.length)] + (1000 + rand.nextInt(8999));
                String type = TYPES[rand.nextInt(TYPES.length)];
                String spot = "F" + (1 + rand.nextInt(3)) + "-R" + (1 + rand.nextInt(5)) + "-S" + (1 + rand.nextInt(10));
                
                // 2. Random Time Logic (Last 7 days)
                LocalDateTime entry = LocalDateTime.now().minusDays(rand.nextInt(7)).minusHours(rand.nextInt(12));
                
                // 3. Status Logic: 80% have left, 20% are still parked (Active)
                LocalDateTime exit = null;
                boolean isStillParked = rand.nextInt(100) < 20; 

                if (!isStillParked) {
                    // They stayed between 30 mins to 10 hours
                    exit = entry.plusMinutes(30 + rand.nextInt(600)); 
                }

                // 4. Create Ticket ID (e.g., T-WAA1234-1030)
                String ticketID = "T-" + plate + "-" + entry.getHour() + entry.getMinute();

                // 5. Set Values
                pstmt.setString(1, ticketID);
                pstmt.setString(2, plate);
                pstmt.setString(3, spot);
                pstmt.setString(4, type);
                pstmt.setString(5, entry.format(formatter));
                
                if (exit != null) {
                    pstmt.setString(6, exit.format(formatter));
                } else {
                    // Important for "Active Vehicles" tab
                    pstmt.setString(6, null); 
                }

                pstmt.addBatch(); // Batch for speed [cite: 2026-02-13]
            }

            // Execute all inserts
            int[] result = pstmt.executeBatch();
            System.out.println("SUCCESS: Inserted " + result.length + " rows into 'vehicle_logs'.");

        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}