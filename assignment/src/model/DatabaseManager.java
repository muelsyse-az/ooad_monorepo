package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

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
}