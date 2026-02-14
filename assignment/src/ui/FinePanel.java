package ui;

import com.formdev.flatlaf.FlatDarkLaf;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import model.FineManager;
import model.DatabaseManager;
import model.Fine;

public class FinePanel extends JPanel {
    private JTextField txtPlate;
    private DefaultTableModel tableModel;
    private JTable fineTable;

    public FinePanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP SECTION: SEARCH ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Vehicle Plate:"));
        txtPlate = new JTextField(15);
        JButton btnSearch = new JButton("Search");
        JButton btnRevoke = new JButton("Revoke Fine");
        btnRevoke.putClientProperty("JButton.buttonType", "borderless");
        btnRevoke.setForeground(Color.RED);
        searchPanel.add(txtPlate);
        searchPanel.add(btnSearch);
        searchPanel.add(btnRevoke);
        add(searchPanel, BorderLayout.NORTH);

        // --- CENTER SECTION: THE TABLE ---
        String[] columns = {"ID", "Plate", "Amount", "Reason", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0)
        {
            @Override
            public boolean isCellEditable(int row, int column) {
                // This makes all cells uneditable
                return false;
            }
        };
        fineTable = new JTable(tableModel);
        
        // Use a JScrollPane so the table is scrollable
        add(new JScrollPane(fineTable), BorderLayout.CENTER);

        // --- BOTTOM SECTION: ACTIONS ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPay = new JButton("Process Payment");
        actionPanel.add(btnPay);
        add(actionPanel, BorderLayout.SOUTH);
        JButton btnDetails = new JButton("View Details");
        actionPanel.add(btnDetails);
        btnDetails.addActionListener(e -> {
            // 1. Get the selected row 
            int selectedRow = fineTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a fine from the table first!");
                return;
            }
        
            // 2. Get the Fine ID from the table (Column 0) 
            String fineID = tableModel.getValueAt(selectedRow, 0).toString();
        
            // 3. Find the matching Fine object from your list
            List<Fine> allFines = FineManager.view_all_fines();
            Fine selectedFine = allFines.stream()
                    .filter(f -> f.getFineID().equals(fineID))
                    .findFirst()
                    .orElse(null);
        
            if (selectedFine != null) {
                // 4. Fetch the deep details
                String[] deepData = FineManager.get_dummy_details(fineID);
        
                // 5. Open the Dialog
                // (Frame) SwingUtilities.getWindowAncestor(this) finds your main window automatically
                FineDetailsDialog dialog = new FineDetailsDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), 
                    selectedFine, 
                    deepData
                );
                dialog.setVisible(true);
            }
        });

        actionPanel.add(btnRevoke);
        btnRevoke.addActionListener(e -> {
            int selectedRow = fineTable.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a fine from the table to revoke.");
                return;
            }
        
            // Get the Fine ID (Column 0) for the specific row [cite: 2026-02-13]
            String fineID = tableModel.getValueAt(selectedRow, 0).toString();
        
            // Add a confirmation dialog before deleting [cite: 2026-01-18, 2026-02-13]
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to PERMANENTLY revoke Fine ID: " + fineID + "?", 
                "Confirm Revocation", 
                JOptionPane.YES_NO_OPTION);
        
            if (confirm == JOptionPane.YES_OPTION) {
                // Call your backend function [cite: 2026-01-15, 2026-02-13]
                boolean success = FineManager.delete_fine(fineID); 
        
                if (success) {
                    JOptionPane.showMessageDialog(this, "Fine " + fineID + " revoked successfully.");
                    updateTable(); // This refreshes the UI automatically [cite: 2026-02-13, 2026-02-14]
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to revoke fine. Check database connection.");
                }
            }
        });
        // --- LOGIC ---
        // Search button now triggers the filter
        btnSearch.addActionListener(e -> updateTable());
        
        // Enter key in text field also triggers search
        txtPlate.addActionListener(e -> updateTable());

        btnPay.addActionListener(e -> performPayment());

        // Initial load (shows all because txtPlate is empty)
        updateTable();
    }

    /**
     * This method handles both "View All" and "Filter" logic
     */
    public void updateTable() {
        tableModel.setRowCount(0); // Clear table
        String filterPlate = txtPlate.getText().trim().toUpperCase();
        
        // Reuse your existing original function!
        List<Fine> allFines = FineManager.view_all_fines(); 
    
        if (allFines != null) {
            for (Fine f : allFines) {
                // If input is empty, show all. Otherwise, check if plate matches.
                if (filterPlate.isEmpty() || f.getVehiclePlate().toUpperCase().contains(filterPlate)) {
                    tableModel.addRow(new Object[]{
                        f.getFineID(),
                        f.getVehiclePlate(),
                        "RM " + String.format("%.2f", f.getAmount()),
                        f.getReason(),
                        f.isPaid() ? "PAID" : "UNPAID",
                        DatabaseManager.formatDateTime(f.getIssueDate())
                    });
                }
            }
        }
    }

    private void performPayment() {
        int selectedRow = fineTable.getSelectedRow();
        
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a fine from the table first!");
            return;
        }
    
        // Get the ID and Plate from the selected row (Column 0 is ID, Column 1 is Plate)
        String fineID = tableModel.getValueAt(selectedRow, 0).toString();
        String plate = tableModel.getValueAt(selectedRow, 1).toString();
        String status = tableModel.getValueAt(selectedRow, 4).toString();
    
        // Prevent paying for something already paid
        if (status.equalsIgnoreCase("PAID")) {
            JOptionPane.showMessageDialog(this, "This fine has already been paid.");
            return;
        }
    
        // Call your FineManager logic
        FineManager.process_payment(plate, "System Edits"); 
        
        JOptionPane.showMessageDialog(this, "Payment successful for Fine ID: " + fineID);
        
        // Refresh the table to show the new "PAID" status
        updateTable(); 
    }
    public static void main(String[] args) {
        FlatDarkLaf.setup();
        JFrame frame = new JFrame("Fine Management System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FinePanel()); 
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}