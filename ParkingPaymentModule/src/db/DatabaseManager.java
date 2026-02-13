package db;

import model.Payment;
import model.PaymentMethod;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:parking_system.db";

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    public void initTables() {
        String createPayments = """
            CREATE TABLE IF NOT EXISTS payments (
              payment_id TEXT PRIMARY KEY,
              plate TEXT NOT NULL,
              total_due REAL NOT NULL,
              amount_paid REAL NOT NULL,
              change_amount REAL NOT NULL,
              method TEXT NOT NULL,
              payment_time TEXT NOT NULL
            );
        """;

        // Only needed if your team doesn't already create fines table.
        String createFines = """
            CREATE TABLE IF NOT EXISTS fines (
              fine_id INTEGER PRIMARY KEY AUTOINCREMENT,
              plate TEXT NOT NULL,
              fine_amount REAL NOT NULL,
              reason TEXT,
              is_paid INTEGER DEFAULT 0,
              payment_method TEXT,
              payment_date TEXT
            );
        """;

        try (Connection c = connect();
             Statement st = c.createStatement()) {
            st.execute(createPayments);
            st.execute(createFines);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePayment(Payment p) throws SQLException {
        String sql = """
            INSERT INTO payments(payment_id, plate, total_due, amount_paid, change_amount, method, payment_time)
            VALUES(?,?,?,?,?,?,?);
        """;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getPaymentId());
            ps.setString(2, p.getPlate());
            ps.setDouble(3, p.getTotalDue());
            ps.setDouble(4, p.getAmountPaid());
            ps.setDouble(5, p.getChangeAmount());
            ps.setString(6, p.getMethod().name());
            ps.setString(7, p.getPaymentTime().toString());
            ps.executeUpdate();
        }
    }

    public int markFinesPaid(String plate, PaymentMethod method, LocalDateTime paidAt) throws SQLException {
        String sql = """
            UPDATE fines
            SET is_paid = 1, payment_method = ?, payment_date = ?
            WHERE plate = ? AND is_paid = 0;
        """;
        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, method.name());
            ps.setString(2, paidAt.toString());
            ps.setString(3, plate);
            return ps.executeUpdate();
        }
    }
}
