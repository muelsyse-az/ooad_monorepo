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
//  Fine Table Operations
//------------------------------------------------------------------------------------------------------------------------------
    
    public static void initialize_fine_table() {
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

    public static Fine get_fine(String vehiclePlate, boolean isPaidStatus) {
        // Translate Java boolean to SQLite integer (true = 1, false = 0)
        int sqlIsPaid = isPaidStatus ? 1 : 0;

        // ORDER BY ensures we get the most recent fine if they have a history of them
        String sql = "SELECT * FROM fines WHERE vehiclePlate = ? AND isPaid = ? ORDER BY issueDate DESC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, vehiclePlate);
            pstmt.setInt(2, sqlIsPaid); // Inject the 1 or 0
            
            ResultSet rs = pstmt.executeQuery();

            // If we find a match in the database...
            if (rs.next()) {
                String id = rs.getString("fineID");
                double amount = rs.getDouble("amount");
                String reason = rs.getString("reason");
                String type = rs.getString("fineSchemeType");
                String issueDateStr = rs.getString("issueDate");
                System.out.println("DEBUG: Date from DB is " + issueDateStr);

                // Grab the exact boolean state from the database to pass to your constructor
                boolean dbIsPaid = rs.getInt("isPaid") == 1;


                // Rebuild the object using your new, flexible constructor!
                return Fine.load_existing(id, vehiclePlate, amount, reason, type, dbIsPaid, issueDateStr); 
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving fine: " + e.getMessage());
        }
        
        // Returns null if no matching fine exists for that plate and status
        return null; 
    }

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

    // DANGER: This deletes ALL fine table data. Use only for testing!
    public static void clear_fines_table() {
        String sql = "DELETE FROM fines"; // "TRUNCATE" isn't standard in SQLite, so we use DELETE

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("   [SYSTEM WARNING] DATABASE CLEARED! All fine records deleted.");

        } catch (SQLException e) {
            System.out.println("Error clearing database: " + e.getMessage());
        }
    }

    public static boolean delete_fine(String fineID) {
        String sql = "DELETE FROM fines WHERE fineID = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fineID);
            int rowsAffected = pstmt.executeUpdate();

            // If rowsAffected > 0, it means we successfully found and deleted it
            return rowsAffected > 0;

        } catch (SQLException e) {
            System.out.println("Error deleting fine: " + e.getMessage());
            return false;
        }
    }

    // ADMIN: View EVERY fine in the database
    public static void view_all_fines() {
        String sql = "SELECT * FROM fines ORDER BY issueDate DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n====================================================================================================");
            System.out.println("                                      MASTER FINE RECORD                                           ");
            System.out.println("====================================================================================================");
            System.out.printf("%-10s %-10s %-12s %-20s %-20s %-10s %-15s\n", 
                "FINE ID", "PLATE", "AMOUNT", "REASON", "ISSUE DATE", "STATUS", "PAY DATE");
            System.out.println("----------------------------------------------------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                
                // 1. Format Status
                String status = (rs.getInt("isPaid") == 1) ? "PAID" : "UNPAID";
                
                // 2. Format Issue Date (Handle potential nulls safely)
                String rawIssue = rs.getString("issueDate");
                String issueDisplay = (rawIssue != null && rawIssue.length() >= 16) 
                                      ? rawIssue.substring(0, 16).replace("T", " ") 
                                      : "N/A";

                // 3. Format Payment Date
                String rawPay = rs.getString("paymentDate");
                String payDisplay = (rawPay != null && rawPay.length() >= 16) 
                                    ? rawPay.substring(0, 16).replace("T", " ") 
                                    : "-";

                System.out.printf("%-10s %-10s RM %-9.2f %-20s %-20s %-10s %-15s\n", 
                    rs.getString("fineID"),
                    rs.getString("vehiclePlate"),
                    rs.getDouble("amount"),
                    rs.getString("reason"),
                    issueDisplay,
                    status,
                    payDisplay
                );
            }

            if (!found) System.out.println("   (Database is empty. No fines recorded.)");
            System.out.println("====================================================================================================\n");

        } catch (SQLException e) {
            System.out.println("Error retrieving Master Record: " + e.getMessage());
        }
    }

    // ADMIN: Show everyone who currently owes money
    public static void view_all_unpaid_fines() {
        String sql = "SELECT * FROM fines WHERE isPaid = 0";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- ADMIN REPORT: OUTSTANDING FINES ---");
            System.out.printf("%-10s %-12s %-10s %-20s\n", "FINE ID", "PLATE", "AMOUNT", "REASON");
            System.out.println("-------------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                System.out.printf("%-10s %-12s RM %-7.2f %-20s\n", 
                    rs.getString("fineID"),
                    rs.getString("vehiclePlate"),
                    rs.getDouble("amount"),
                    rs.getString("reason")
                );
            }

            if (!found) System.out.println("   (No outstanding fines found. Good job!)");
            System.out.println("-------------------------------------------------------\n");

        } catch (SQLException e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }

    // ADMIN: Show full history for one car (Paid and Unpaid)
    public static void view_vehicle_fine_history(String plate) {
        String sql = "SELECT * FROM fines WHERE vehiclePlate = ? ORDER BY issueDate DESC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, plate);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("\n--- VIOLATION HISTORY: " + plate + " ---");
            System.out.printf("%-10s %-12s %-10s %-10s\n", "FINE ID", "DATE", "AMOUNT", "STATUS");
            System.out.println("------------------------------------------------");

            boolean found = false;
            while (rs.next()) {
                found = true;
                String status = (rs.getInt("isPaid") == 1) ? "[PAID]" : "[OWING]";
                
                String rawDate = rs.getString("issueDate");
                String displayDate = (rawDate != null && rawDate.length() >= 10) 
                                 ? rawDate.substring(0, 10) 
                                 : "N/A";
                
                System.out.printf("%-10s %-12s RM %-7.2f %-10s\n", 
                    rs.getString("fineID"),
                    displayDate,
                    rs.getDouble("amount"),
                    status
                );
            }

            if (!found) System.out.println("   (No record found. This driver is clean.)");
            System.out.println("------------------------------------------------\n");

        } catch (SQLException e) {
            System.out.println("Error retrieving history: " + e.getMessage());
        }
    }
}
