package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import model.Fine;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DatabaseManager 
{
    // This will create a local file named "ParkingSystem.db" right in your folder
    private static final String DB_URL = "jdbc:sqlite:ParkingSystem.db";

    // 1. Establish the connection to the database
    public static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DB_URL);
        } catch (SQLException e) {
            System.out.println("Connection Failed: " + e.getMessage());
        }
        return conn;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "N/A";
        }
        // Choosing a clean format: Day-Month-Year Hour:Minute AM/PM [cite: 2026-02-14]
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");
        return dateTime.format(formatter);
    }

    //------------------------------------------------------------------------------------------------------------------------------
    //  TICKET Table Operations (Add this section to DatabaseManager.java)
    //------------------------------------------------------------------------------------------------------------------------------

    public static void initialize_ticket_table() {
        String sql = "CREATE TABLE IF NOT EXISTS tickets ("
                + "ticketID TEXT PRIMARY KEY, "
                + "vehiclePlate TEXT UNIQUE, " // One active ticket per car
                + "spotID TEXT, "
                + "vehicleType TEXT, "
                + "entryTime TEXT"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("SUCCESS: 'tickets' table is ready.");
        } catch (SQLException e) {
            System.out.println("Error initializing ticket table: " + e.getMessage());
        }
    }

    public static void save_ticket(Ticket t) {
        String sql = "INSERT OR REPLACE INTO tickets (ticketID, vehiclePlate, spotID, vehicleType, entryTime) VALUES(?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, t.getTicketID());
            pstmt.setString(2, t.getVehiclePlate());
            pstmt.setString(3, t.getSpotID());
            pstmt.setString(4, t.getVehicleType());
            pstmt.setString(5, t.getEntryTime().toString());

            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Error saving ticket: " + e.getMessage());
        }
    }

    public static Ticket get_active_ticket(String plate) {
        String sql = "SELECT * FROM tickets WHERE vehiclePlate = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, plate);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Ticket.load_existing(
                    rs.getString("ticketID"),
                    rs.getString("vehiclePlate"),
                    rs.getString("spotID"),
                    rs.getString("vehicleType"),
                    rs.getString("entryTime")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching ticket: " + e.getMessage());
        }
        return null;
    }

    public static boolean delete_ticket(String plate) {
        String sql = "DELETE FROM tickets WHERE vehiclePlate = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, plate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    //------------------------------------------------------------------------------------------------------------------------------
    //  SPOT Table Operations
    //------------------------------------------------------------------------------------------------------------------------------

    public static void initialize_spot_table() {
        String sql = "CREATE TABLE IF NOT EXISTS spots ("
                + "spotID TEXT PRIMARY KEY, "
                + "floor INTEGER, "
                + "row INTEGER, "
                + "type TEXT, "
                + "isOccupied INTEGER DEFAULT 0"
                + ");";
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Error init spots: " + e.getMessage());
        }
    }

    // Save or Update a spot's status
    public static void update_spot_status(String spotID, boolean isOccupied) {
        String sql = "UPDATE spots SET isOccupied = ? WHERE spotID = ?";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, isOccupied ? 1 : 0);
            pstmt.setString(2, spotID);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating spot: " + e.getMessage());
        }
    }

    // Used to populate the lot initially
    public static void save_spot(ParkingSpot spot) {
        String sql = "INSERT OR REPLACE INTO spots (spotID, floor, row, type, isOccupied) VALUES(?,?,?,?,?)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, spot.getSpotID());
            pstmt.setInt(2, spot.getFloor());
            pstmt.setInt(3, spot.getRow());
            pstmt.setString(4, spot.getType().toString());
            pstmt.setInt(5, spot.isOccupied() ? 1 : 0);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving spot: " + e.getMessage());
        }
    }

    // Find a spot that matches type and is free
    public static String find_available_spot(String type) {
        // Simple logic: Find first free spot of this exact type
        String sql = "SELECT spotID FROM spots WHERE type = ? AND isOccupied = 0 LIMIT 1";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getString("spotID");
        } catch (SQLException e) {
            System.out.println("Error finding spot: " + e.getMessage());
        }
        return null; // Lot full for this type
    }

    public static void initialize_vehicle_logs_table() {
        String sql = "CREATE TABLE IF NOT EXISTS vehicle_logs ("
                + "logID INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "ticketID TEXT, "
                + "vehiclePlate TEXT, "
                + "spotID TEXT, "
                + "vehicleType TEXT, "
                + "entryTime TEXT, "
                + "exitTime TEXT"
                + ");";
    
        // The catch block below handles errors from connect(), execute(), AND the hidden close()
        try (Connection conn = connect(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("SUCCESS: 'vehicle_logs' table is ready.");
        } catch (SQLException e) {
            System.err.println("Database Error: " + e.getMessage());
            // For your MMU eBus project, logging the error is crucial for debugging [cite: 2026-01-22]
        }
    }

            
    public static void save_vehicle_log(VehicleLog log) {
        String sql = "INSERT INTO vehicle_logs(ticketID, vehiclePlate, spotID, vehicleType, entryTime, exitTime) "
                    + "VALUES(?,?,?,?,?,?)";
    
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
    
            pstmt.setString(1, log.getTicketID());
            pstmt.setString(2, log.getVehiclePlate());
            pstmt.setString(3, log.getSpotID());
            pstmt.setString(4, log.getVehicleType());
            
            // Use your new formatDateTime utility to keep things consistent [cite: 2026-02-14]
            pstmt.setString(5, DatabaseManager.formatDateTime(log.getEntryTime()));
            
            // Null check for exitTime to prevent crashes [cite: 2026-02-14]
            if (log.getExitTime() != null) {
                pstmt.setString(6, DatabaseManager.formatDateTime(log.getExitTime()));
            } else {
                pstmt.setNull(6, java.sql.Types.VARCHAR); 
            }
    
            pstmt.executeUpdate(); // IMPORTANT: You forgot to actually execute the statement! [cite: 2026-01-15]
            System.out.println("Log saved for: " + log.getVehiclePlate());
    
        } catch (SQLException e) {
            // This catch block is mandatory [cite: 2026-02-06, 2026-02-13]
            System.err.println("Error saving vehicle log: " + e.getMessage());
        }
    }

}


        