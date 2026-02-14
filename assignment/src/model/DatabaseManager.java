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
    public static Fine get_fine(String plate, boolean isPaid) {
        String sql = "SELECT * FROM fines WHERE vehiclePlate = ? AND isPaid = ? LIMIT 1";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plate);
            pstmt.setInt(2, isPaid ? 1 : 0);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return Fine.load_existing(
                        rs.getString("fineID"),
                        rs.getString("vehiclePlate"),
                        rs.getDouble("amount"),
                        rs.getString("reason"),
                        rs.getString("fineSchemeType"),
                        rs.getInt("isPaid") == 1,
                        rs.getString("issueDate")
                );
            }
        } catch (SQLException e) {
            System.out.println("Error fetching fine: " + e.getMessage());
        }
        return null;
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

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("SUCCESS: 'vehicle_logs' table is ready.");
        } catch (SQLException e) {
            System.out.println("Error initializing vehicle_logs table: " + e.getMessage());
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
            pstmt.setString(5, log.getEntryTime().toString());
            pstmt.setString(6, log.getExitTime().toString());

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error saving vehicle log: " + e.getMessage());
        }
    }
}