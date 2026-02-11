package Models;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class DatabaseManager 
{
    // This will create a local file named "parking_system.db" right in your folder
    private static final String DB_URL = "jdbc:sqlite:parking_system.db";

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

    // 2. Create the Fines table to match our Java Model
    public static void initialize_database() {
        // We write standard SQL here. 
        // Note: SQLite uses INTEGER (0 or 1) for booleans, and TEXT for dates.
        String createFinesTableSQL = "CREATE TABLE IF NOT EXISTS fines ("
                + "fineID TEXT PRIMARY KEY, "
                + "fineSchemeType TEXT, "
                + "vehiclePlate TEXT NOT NULL, "
                + "amount REAL NOT NULL, "
                + "reason TEXT, "
                + "issueDate TEXT, "
                + "isPaid INTEGER DEFAULT 0, "
                + "paymentDate TEXT, "
                + "paymentMethod TEXT"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            // Execute the SQL statement
            stmt.execute(createFinesTableSQL);
            System.out.println("SUCCESS: Database initialized and 'fines' table is ready.");
            
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

//------------------------------------------------------------------------------------------------------------------------------
//  Fine Operations
//------------------------------------------------------------------------------------------------------------------------------

    public static void save_fine(Fine fine) 
    {
        // SQLite superpower: INSERT OR REPLACE handles both new fines AND updating paid fines!
        String sql = "INSERT OR REPLACE INTO fines "
                    + "(fineID, fineSchemeType, vehiclePlate, amount, reason, issueDate, isPaid, paymentDate, paymentMethod) "
                    + "VALUES(?,?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
                PreparedStatement pstmt = conn.prepareStatement(sql)) 
        {
            
            // Fill in the "?" placeholders with data from our Fine object
            pstmt.setString(1, fine.getFineID());
            pstmt.setString(2, fine.getFineSchemeType());
            pstmt.setString(3, fine.getVehiclePlate());
            pstmt.setDouble(4, fine.getAmount());
            pstmt.setString(5, fine.getReason());
            
            // SQLite stores dates as Text, so we convert LocalDateTime to a String
            pstmt.setString(6, fine.getIssueDate() != null ? fine.getIssueDate().toString() : null);
            
            // SQLite stores booleans as 0 (false) or 1 (true)
            pstmt.setInt(7, fine.isPaid() ? 1 : 0);
            
            pstmt.setString(8, fine.getPaymentDate() != null ? fine.getPaymentDate().toString() : null);
            pstmt.setString(9, fine.getPaymentMethod());

            // Execute the save!
            pstmt.executeUpdate();
            System.out.println("   [DB SUCCESS] Saved fine " + fine.getFineID() + " to SQLite.");

        } catch (SQLException e) 
        {
            System.out.println("Error saving fine to database: " + e.getMessage());
        }
    }

    public static void generate_fine_revenue_report(String dateFilter) {
        // Uses the LIKE operator to match the beginning of our date strings (e.g., "2026-02-11%")
        String sql = "SELECT SUM(amount) as total_revenue, COUNT(fineID) as total_fines " +
                     "FROM fines WHERE isPaid = 1 AND paymentDate LIKE ?";
    
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, dateFilter + "%");
            ResultSet rs = pstmt.executeQuery();
    
            if (rs.next()) {
                double totalRevenue = rs.getDouble("total_revenue");
                int totalFines = rs.getInt("total_fines");
    
                System.out.println("\n========================================");
                System.out.println("   FINE REVENUE REPORT - DATE: " + dateFilter);
                System.out.println("========================================");
                System.out.println(" Total Fines Collected : " + totalFines);
                System.out.println(" Total Revenue Earned  : RM " + String.format("%.2f", totalRevenue));
                System.out.println("========================================\n");
            }
    
        } catch (SQLException e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
}
