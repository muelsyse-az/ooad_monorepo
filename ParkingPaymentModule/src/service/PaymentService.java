package service;

import db.DatabaseManager;
import model.Payment;
import model.PaymentMethod;
import model.Receipt;

import java.time.LocalDateTime;

public class PaymentService {
    private final DatabaseManager db;

    public PaymentService(DatabaseManager db) {
        this.db = db;
    }

    public Receipt processPayment(String plate, double totalDue, PaymentMethod method, double amountPaid,
                                  boolean payFines) throws Exception {

        if (plate == null || plate.trim().isEmpty()) throw new IllegalArgumentException("Plate required.");
        if (method == null) throw new IllegalArgumentException("Select payment method.");

        double change = 0;
        if (method == PaymentMethod.CASH) {
            if (amountPaid < totalDue) throw new IllegalArgumentException("Insufficient cash.");
            change = amountPaid - totalDue;
        } else {
            amountPaid = totalDue; // card pays exact
        }

        LocalDateTime now = LocalDateTime.now();
        String paymentId = "PAY-" + plate + "-" + System.currentTimeMillis();

        Payment payment = new Payment(paymentId, plate, totalDue, amountPaid, change, method, now);

        db.initTables();
        db.savePayment(payment);

        if (payFines) db.markFinesPaid(plate, method, now);

        String receiptText = """
                ===== PAYMENT RECEIPT =====
                Payment ID: %s
                Plate     : %s
                Method    : %s
                Total Due : RM %.2f
                Paid      : RM %.2f
                Change    : RM %.2f
                Time      : %s
                ===========================
                """.formatted(paymentId, plate, method, totalDue, amountPaid, change, now);

        return new Receipt(receiptText);
    }
}
