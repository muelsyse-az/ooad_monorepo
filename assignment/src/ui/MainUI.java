package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import java.awt.*;

public class MainUI {

    public static void main(String[] args) {
        // 1. SETUP LOOK AND FEEL
        try {
            FlatDarkLaf.setup(); // Modern Dark Theme
            UIManager.put("Button.arc", 10); // Rounded buttons
            UIManager.put("Component.arc", 10);
            System.out.println("Success: FlatLaf initialized.");
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        // 2. LAUNCH APPLICATION
        SwingUtilities.invokeLater(() -> {
            createMainFrame();
        });
    }

    private static void createMainFrame() {
        JFrame frame = new JFrame("Parking Lot Management System - Group X");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLocationRelativeTo(null); // Center on screen

        // --- MAIN TABS ---
        JTabbedPane mainTabs = new JTabbedPane();

        // TAB 1: Entry / Exit Panel (Stub for now)
        JPanel entryExitStub = new JPanel(new BorderLayout());
        entryExitStub.add(new JLabel("Entry/Exit Interface Coming Soon...", SwingConstants.CENTER));
        mainTabs.addTab("Entry & Exit Operations", new ImageIcon(), entryExitStub, "Process Vehicles");

        // TAB 2: Admin Dashboard (The class we just made)
        AdminPanel adminPanel = new AdminPanel();
        mainTabs.addTab("Admin Dashboard", new ImageIcon(), adminPanel, "View Reports & Settings");

        frame.add(mainTabs);
        frame.setVisible(true);
    }
}