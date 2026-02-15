package model;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class VehicleLogManager {

    // --- 1. LOG ENTRY (The "Check-In" Process) ---
    public static boolean log_vehicle_entry(String ticketID, String plate, String spotID, String type) {
        String sql = "INSERT INTO vehicle_logs(ticketID, vehiclePlate, spotID, vehicleType, entryTime) VALUES(?,?,?,?,?)";
        
        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, ticketID);
            pstmt.setString(2, plate);
            pstmt.setString(3, spotID);
            pstmt.setString(4, type);
            // Use standard format for DB consistency [cite: 2026-02-14]
            pstmt.setString(5, DatabaseManager.formatDateTime(LocalDateTime.now()));

            pstmt.executeUpdate();
            System.out.println("ENTRY LOGGED: " + plate + " at " + spotID);
            return true;

        } catch (SQLException e) {
            System.err.println("Error logging entry: " + e.getMessage());
            return false;
        }
    }

    // --- 2. LOG EXIT (The "Check-Out" Process) ---
    public static boolean update_vehicle_exit(String ticketID) {
        // We only update the exitTime. The entryTime stays the same.
        String sql = "UPDATE vehicle_logs SET exitTime = ? WHERE ticketID = ?";

        try (Connection conn = DatabaseManager.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String timeNow = DatabaseManager.formatDateTime(LocalDateTime.now());
            pstmt.setString(1, timeNow);
            pstmt.setString(2, ticketID);

            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0; // Returns true if a record was actually updated

        } catch (SQLException e) {
            System.err.println("Error updating exit log: " + e.getMessage());
            return false;
        }
    }

    // --- 3. FETCH ACTIVE VEHICLES (For your "Active Vehicles" Tab) ---
    public static List<VehicleLog> get_active_logs() {
        // "Active" means exitTime is NULL
        String sql = "SELECT * FROM vehicle_logs WHERE exitTime IS NULL";
        return fetch_logs(sql);
    }

    // --- 4. FETCH HISTORY (For "Financial Reports" or history checks) ---
    public static List<VehicleLog> get_all_logs() {
        String sql = "SELECT * FROM vehicle_logs ORDER BY entryTime DESC";
        return fetch_logs(sql);
    }

    // --- 5. FIND SPECIFIC LOG ---
    public static VehicleLog get_log_by_ticket(String ticketID) {
        String sql = "SELECT * FROM vehicle_logs WHERE ticketID = '" + ticketID + "'";
        List<VehicleLog> results = fetch_logs(sql);
        return results.isEmpty() ? null : results.get(0);
    }

    // --- HELPER: CENTRALIZED ROW MAPPING ---
    private static List<VehicleLog> fetch_logs(String query) {
        List<VehicleLog> logs = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                // Parse Dates safely
                String entryStr = rs.getString("entryTime");
                String exitStr = rs.getString("exitTime");
                
                LocalDateTime entry = LocalDateTime.parse(entryStr, formatter);
                LocalDateTime exit = (exitStr != null) ? LocalDateTime.parse(exitStr, formatter) : null;

                logs.add(new VehicleLog(
                    rs.getString("ticketID"),
                    rs.getString("vehiclePlate"),
                    rs.getString("spotID"),
                    rs.getString("vehicleType"),
                    entry,
                    exit
                ));
            }
        } catch (Exception e) {
            System.err.println("Error fetching logs: " + e.getMessage());
        }
        return logs;
    }
}
