package ui;

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

        // 2. LAUNCH APPLICATION
        SwingUtilities.invokeLater(() -> {
            // For now, we just open a test frame since we have no real UI yet
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

        JButton btnEntry = new JButton("Simulate Entry");
        JButton btnExit = new JButton("Simulate Exit");
        JTextField txtPlate = new JTextField("WAA1234", 15);
        JLabel lblStatus = new JLabel("Status: Waiting...");
        JCheckBox chkReceipt = new JCheckBox("Print Receipt");

        panel.add(new JLabel("Vehicle Plate:"));
        panel.add(txtPlate);
        panel.add(chkReceipt);
        panel.add(btnEntry);
        panel.add(btnExit);
        panel.add(lblStatus);

        frame.add(panel);
        frame.setVisible(true);
    }
}