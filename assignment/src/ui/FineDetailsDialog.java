package ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

import model.DatabaseManager;
import model.Fine;

public class FineDetailsDialog extends JDialog {
    public FineDetailsDialog(Frame owner, Fine fine, String[] dummyData) {
        
        super(owner, "Deep Details - " + fine.getFineID(), true);
        setLayout(new BorderLayout());
        
        JPanel contentPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        contentPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Data from your original Fine table
        addRow(contentPanel, "Fine ID:", fine.getFineID());
        addRow(contentPanel, "Vehicle Plate:", fine.getVehiclePlate());
        addRow(contentPanel, "Base Amount:", "RM " + fine.getAmount());
        addRow(contentPanel, "Reason:", fine.getReason());
        addRow(contentPanel, "Issue Date:", DatabaseManager.formatDateTime(fine.getIssueDate()));

        // Data from the Dummy Table (overtime, etc.)
        addRow(contentPanel, "Overtime (hours):", dummyData[0]);
        String payment_method = fine.getPaymentMethod();
        if (payment_method == null || payment_method.isEmpty()) {
            payment_method = "N/A";
        }
        addRow(contentPanel, "Payment Method:", payment_method);
        addRow(contentPanel, "Payment Date:", DatabaseManager.formatDateTime(fine.getPaymentDate()));
        addRow(contentPanel, "Staff In-Charge:", dummyData[3]);

        add(contentPanel, BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        add(btnClose, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(owner);
    }

    private void addRow(JPanel panel, String label, Object value) {
        panel.add(new JLabel("<html><b>" + label + "</b></html>"));
        // String.valueOf() safely converts nulls or any object type to a String
        panel.add(new JLabel(String.valueOf(value)));
    }
}