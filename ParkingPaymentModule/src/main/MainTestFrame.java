package main;

import ui.PaymentPanel;
import db.DatabaseManager;

import javax.swing.*;

public class MainTestFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            DatabaseManager db = new DatabaseManager();

            JFrame frame = new JFrame("Payment Module Test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            PaymentPanel panel = new PaymentPanel(db);
            panel.setBill("ABC1234", 25.00); // sample plate + amount

            frame.setContentPane(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
