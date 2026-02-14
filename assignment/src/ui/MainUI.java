package ui;

import model.FineManager;
import model.DatabaseManager;
import model.Fine;
import java.util.List;

import com.formdev.flatlaf.FlatDarkLaf; // or FlatLightLaf
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class MainUI {

    public static void main(String[] args) {
        // 1. SETUP LOOK AND FEEL
        try {
            // Choose your theme here:
            // FlatLightLaf.setup();  // Clean Light Theme
            FlatDarkLaf.setup();      // Modern Dark Theme
            // FlatIntelliJLaf.setup(); // Like IntelliJ IDEA Light
            // FlatDarculaLaf.setup();  // Like IntelliJ IDEA Dark

            System.out.println("Success: FlatLaf initialized.");
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> {
            DatabaseManager.initialize_ticket_table();
            DatabaseManager.initialize_vehicle_logs_table();
            // (If your fines table has an init method, add it too)
            // DatabaseManager.initialize_fines_table();

            createTestFrame();
        });

    }

    // Temporary method to verify it works
    private static void createTestFrame() {
        JFrame frame = new JFrame("Parking System - FlatLaf Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLocationRelativeTo(null); // Center on screen

        // Create a simple panel with some components to see the styling
        JPanel panel = new JPanel();

        JTextField txtPlate = new JTextField("WAA1234", 15);
        JButton btnCheck = new JButton("Check Unpaid Fines");
        JButton btnPay = new JButton("Pay Fine");
        JLabel lblStatus = new JLabel("Enter a plate to check status.");

        // 1. Action for the "Check" button
    btnCheck.addActionListener(e -> {
        String plate = txtPlate.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Please enter a plate number!");
            return;
        }

        // Call your backend method!
        Fine unpaid = DatabaseManager.get_fine(plate, false); 

        if (unpaid != null) {
            lblStatus.setText("<html><font color='red'><b>UNPAID: RM " + 
                             unpaid.getAmount() + "</b></font> (ID: " + unpaid.getFineID() + ")</html>");
        } else {
            lblStatus.setText("Status: No active fines for " + plate);
        }
    });

    // 2. Action for the "Pay" button
    btnPay.addActionListener(e -> {
        String plate = txtPlate.getText().trim();
        // Trigger your payment logic
        FineManager.process_payment(plate, "Cash"); 
        
        // Give feedback to user
        JOptionPane.showMessageDialog(frame, "Payment processed for " + plate);
        lblStatus.setText("Status: Payment Recorded.");
    });

    panel.add(new JLabel("Vehicle Plate:"));
    panel.add(txtPlate);
    panel.add(btnCheck);
    panel.add(btnPay);
    panel.add(lblStatus);

        frame.add(panel);
        frame.setVisible(true);
    }
}