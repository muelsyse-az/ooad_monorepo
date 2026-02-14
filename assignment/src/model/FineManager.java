package model;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FineManager {

    //-----------------------------------------------------------------------------------------------------------------------------
    // Database Method
    //-----------------------------------------------------------------------------------------------------------------------------

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

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {
            
            // Execute the SQL statement
            stmt.execute(createFinesTableSQL);
            System.out.println("SUCCESS: Database initialized and 'fines' table is ready.");
            
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

//------------------------------------------------------------------------------------------------------------------------------
//  Only For testing FinePanel UI
//------------------------------------------------------------------------------------------------------------------------------
    
    public static void initialize_dummydataforfine_table() 
    {
        // We write standard SQL here. 
        // Note: SQLite uses INTEGER (0 or 1) for booleans, and TEXT for dates.
        String createFinesTableSQL = "CREATE TABLE IF NOT EXISTS DummyDataforFine ("
                + "fineID TEXT PRIMARY KEY, "
                + "overtimeAmount REAL,"
                + "paymentWay TEXT,"
                + "paymentDate TEXT,"
                + "staffInCharge TEXT,"
                + "FOREIGN KEY(fineID) REFERENCES fines(fineID)"
                + ");";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {
            
            // Execute the SQL statement
            stmt.execute(createFinesTableSQL);
            System.out.println("SUCCESS: Database initialized and 'DummyDataforFine' table is ready.");
            
        } catch (SQLException e) {
            System.out.println("Error creating table: " + e.getMessage());
        }
    }

    public static String[] get_dummy_details(String fineID) {
        String sql = "SELECT * FROM DummyDataforFine WHERE fineID = ?";
        try (Connection conn = DatabaseManager.connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, fineID);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new String[]{
                    String.valueOf(rs.getDouble("overtimeAmount")),
                    rs.getString("paymentWay"),
                    rs.getString("paymentDate"),
                    rs.getString("staffInCharge")
                };
            }
        } catch (SQLException e) {
            System.err.println("Error fetching dummy data: " + e.getMessage());
        }
        // Fallback if no dummy data exists yet [cite: 2026-02-14]
        return new String[]{"0.00", "N/A", "N/A", "System"};
    }



//---------------------------------------------------------------------------------------------------------------------


    public static Fine get_fine(String vehiclePlate, boolean isPaidStatus) {
        // Translate Java boolean to SQLite integer (true = 1, false = 0)
        int sqlIsPaid = isPaidStatus ? 1 : 0;

        // ORDER BY ensures we get the most recent fine if they have a history of them
        String sql = "SELECT * FROM fines WHERE vehiclePlate = ? AND isPaid = ? ORDER BY issueDate DESC";

        try (Connection conn = DatabaseManager.connect();
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
                String paymentDateStr = rs.getString("paymentDate");
                String paymentMethod = rs.getString("paymentMethod");
                System.out.println("DEBUG: Date from DB is " + issueDateStr);

                // Grab the exact boolean state from the database to pass to your constructor
                boolean dbIsPaid = rs.getInt("isPaid") == 1;


                // Rebuild the object using your new, flexible constructor!
                return Fine.load_existing(id, vehiclePlate, amount, reason, type, dbIsPaid, issueDateStr, paymentDateStr, paymentMethod); 
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

        try (Connection conn = DatabaseManager.connect();
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

    // DANGER: This deletes ALL fine table data. Use only for testing!
    public static void clear_fines_table() {
        String sql = "DELETE FROM fines"; // "TRUNCATE" isn't standard in SQLite, so we use DELETE

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
            System.out.println("   [SYSTEM WARNING] DATABASE CLEARED! All fine records deleted.");

        } catch (SQLException e) {
            System.out.println("Error clearing database: " + e.getMessage());
        }
    }
    
    //-----------------------------------------------------------------------------------------------------------------------------
    // Business Logic Methods
    //----------------------------------------------------------------------------------------------------------------------------- 
    
    public static void generate_fine_revenue_report(String dateFilter) {
        // Uses the LIKE operator to match the beginning of our date strings (e.g., "2026-02-11%")
        String sql = "SELECT SUM(amount) as total_revenue, COUNT(fineID) as total_fines " +
                     "FROM fines WHERE isPaid = 1 AND paymentDate LIKE ?";
    
        try (Connection conn = DatabaseManager.connect();
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
    // ADMIN: View EVERY fine in the database
    public static List<Fine> view_all_fines() {
        List<Fine> list = new ArrayList<>();
        String sql = "SELECT * FROM fines ORDER BY issueDate DESC";

        try (Connection conn = DatabaseManager.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(Fine.load_existing(
                    rs.getString("fineID"),
                    rs.getString("vehiclePlate"),
                    rs.getDouble("amount"),
                    rs.getString("reason"),
                    rs.getString("fineSchemeType"),
                    rs.getInt("isPaid") == 1,
                    rs.getString("issueDate"),
                    rs.getString("paymentDate"),
                    rs.getString("paymentMethod")
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving Master Record: " + e.getMessage());
        }
        return list;
    }

    // ADMIN: Show everyone who currently owes money
    public static void view_all_unpaid_fines() {
        String sql = "SELECT * FROM fines WHERE isPaid = 0";

        try (Connection conn = DatabaseManager.connect();
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

        try (Connection conn = DatabaseManager.connect();
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
    
    public static double calculate_fine_amount(String schemeType, double overstayHours) {
        double amount = 0.0;

        switch (schemeType) {
            case "Fixed":
                // Option A: Flat RM 50
                amount = 50.00;
                break;

            case "Progressive":
                // Option B: Tiered fines
                if (overstayHours <= 24) {
                    amount = 50.00;
                } else if (overstayHours <= 48) {
                    amount = 50.00 + 100.00; // RM 150
                } else if (overstayHours <= 72) {
                    amount = 50.00 + 100.00 + 150.00; // RM 300
                } else {
                    amount = 50.00 + 100.00 + 150.00 + 200.00; // RM 500
                }
                break;

            case "Hourly":
                // Option C: RM 20 per hour (Rounded up to nearest hour usually, but we'll use exact for now)
                // If you want to charge for partial hours (e.g. 1.5 hours = RM 30), keep as is.
                // If you want to charge full hours (e.g. 1.1 hours = 2 hours = RM 40), use Math.ceil(overstayHours)
                amount = Math.ceil(overstayHours) * 20.00;
                break;

            default:
                System.out.println("   [WARNING] Unknown scheme. Defaulting to Fixed.");
                amount = 50.00;
        }
        return amount;
    }

    // --- 1. ISSUE FINE (Updated with Math) ---
    public static void issue_fine(String vehiclePlate, String reason, String schemeType, double overstayHours) {
        // RULE 1: Check if car already has an UNPAID fine
        Fine existingFine = get_fine(vehiclePlate, false);

        if (existingFine != null) {
            // RULE: "If customer leaves without paying... next exit shows unpaid fine"
            // Since it already exists in DB, we don't overwrite it. We just remind them.
            System.out.println("   [ERROR] Vehicle " + vehiclePlate + " already has outstanding fine: " + existingFine.getFineID());
            System.out.println("   Current Amount Due: RM " + existingFine.getAmount());
            return;
        }

        // RULE 2: Calculate Amount based on the chosen scheme
        double finalAmount = calculate_fine_amount(schemeType, overstayHours);

        // Create the object
        Fine newFine = Fine.create_new(vehiclePlate, finalAmount, reason, schemeType);

        // Save it permanently
        save_fine(newFine);
        System.out.println("   [SUCCESS] New fine issued to " + vehiclePlate);
        System.out.println("   Scheme: " + schemeType + " | Hours: " + overstayHours + " | Total: RM " + finalAmount);
    }

    // --- 2. PROCESS PAYMENT (Unchanged) ---
    public static void process_payment(String vehiclePlate, String paymentMethod) {
        Fine fineToPay = get_fine(vehiclePlate, false);

        if (fineToPay == null) {
            System.out.println("   [INFO] No unpaid fines found for " + vehiclePlate);
            return;
        }

        fineToPay.pay(paymentMethod);
        save_fine(fineToPay);
        System.out.println("   [SUCCESS] Payment recorded. Fine " + fineToPay.getFineID() + " is CLEARED.");
    }

    // --- 3. GATE CHECK (Unchanged) ---
    public static boolean is_vehicle_barred(String vehiclePlate) {
        Fine unpaidFine = get_fine(vehiclePlate, false);
        
        if (unpaidFine != null) {
            System.out.println("   [ALERT] GATE BLOCKED! " + vehiclePlate + " owes RM " + unpaidFine.getAmount());
            // This satisfies the requirement: "Next exit will show the unpaid fine"
            return true; 
        }
        return false;
    }

    public static boolean delete_fine(String fineID) {
        String sql = "DELETE FROM fines WHERE fineID = ?";

        try (Connection conn = DatabaseManager.connect();
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

    // --- 4. ADMIN: REVOKE FINE (Undo Mistake) ---
    public static void revoke_fine(String fineID) {
        boolean success = delete_fine(fineID);

        if (success) {
            System.out.println("   [SUCCESS] Fine " + fineID + " has been permanently REVOKED.");
        } else {
            System.out.println("   [ERROR] Could not revoke fine " + fineID + ". ID not found.");
        }
    } 
}