package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

import model.DatabaseManager;
import model.FineManager;

public class AdminPanel extends JPanel {

    // UI Components (Context Variables)
    private JComboBox<String> fineSchemeCombo;
    private JLabel lblTotalRevenue;
    private JLabel lblOccupancyRate;
    private JTable lotStatusTable;
    private JTable currentVehiclesTable;
    private JTable finesTable;

    public AdminPanel() {
        // 1. Layout Manager (Like CSS Grid/Flexbox)
        setLayout(new BorderLayout(15, 15)); // 15px gaps
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // 2. Top Bar: System Configuration
        add(createTopConfigPanel(), BorderLayout.NORTH);

        // 3. Center: The Dashboard Tabs
        JTabbedPane dashboardTabs = new JTabbedPane();
        dashboardTabs.addTab("Lot Status", createLotStatusPanel());
        dashboardTabs.addTab("Active Vehicles", createActiveVehiclesPanel());
        dashboardTabs.addTab("Financial Reports", createFinancialPanel());

        add(dashboardTabs, BorderLayout.CENTER);
    }

    // --- SECTION 1: TOP CONFIG PANEL ---
    private JPanel createTopConfigPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("System Configuration"));
    
        panel.add(new JLabel("Current Fine Scheme:"));
        String[] schemes = {"Option A: Fixed (RM 50)", "Option B: Progressive", "Option C: Hourly"};
        fineSchemeCombo = new JComboBox<>(schemes);
        panel.add(fineSchemeCombo);
    
        JButton btnUpdate = new JButton("Apply New Scheme");
        btnUpdate.addActionListener(this::handleUpdateScheme);
        panel.add(btnUpdate);
    
        // --- NEW: ISSUE FINE BUTTON --- [cite: 2026-02-14]
        JButton btnIssueFine = new JButton("Issue Fine");
        btnIssueFine.putClientProperty("JButton.buttonType", "roundRect");
        btnIssueFine.addActionListener(e -> {
            // For now, this can just show a message, 
            // later you can link it to your 'Issue Fine Dialog' [cite: 2026-01-22]
            JOptionPane.showMessageDialog(this, "Opening Issue Fine wizard...");
        });
        panel.add(btnIssueFine);
    
        lblOccupancyRate = new JLabel("  |  Occupancy: 12% (18/150 Spots)");
        lblOccupancyRate.setFont(lblOccupancyRate.getFont().deriveFont(Font.BOLD));
        panel.add(lblOccupancyRate);
    
        return panel;
    }

    // --- SECTION 2: LOT STATUS TAB [cite: 443] ---
    private JPanel createLotStatusPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Define Table Columns
        String[] columns = {"Floor", "Row", "Spot ID", "Type", "Status", "Current Plate"};

        // Create Model (The Data)
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        lotStatusTable = new JTable(model);

        // STUB DATA: Adding fake rows so you can see the UI
        model.addRow(new Object[]{1, 1, "F1-R1-S1", "HANDICAPPED", "OCCUPIED", "WAA1234"});
        model.addRow(new Object[]{1, 1, "F1-R1-S2", "HANDICAPPED", "AVAILABLE", "-"});
        model.addRow(new Object[]{1, 2, "F1-R2-S1", "COMPACT", "AVAILABLE", "-"});
        model.addRow(new Object[]{2, 1, "F2-R1-S1", "REGULAR", "OCCUPIED", "JDT9999"});

        // Add to ScrollPane (Overflow: scroll)
        panel.add(new JScrollPane(lotStatusTable), BorderLayout.CENTER);
        return panel;
    }

    // --- SECTION 3: ACTIVE VEHICLES TAB [cite: 446] ---
    private JPanel createActiveVehiclesPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columns = {"Ticket ID", "Plate Number", "Spot ID", "Entry Time", "Est. Duration"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        currentVehiclesTable = new JTable(model);

        // STUB DATA
        model.addRow(new Object[]{"T-WAA1234-1000", "WAA1234", "F1-R1-S1", "2026-02-14 10:00 AM", "5 hours"});
        model.addRow(new Object[]{"T-JDT9999-1230", "JDT9999", "F2-R1-S1", "2026-02-14 12:30 PM", "2.5 hours"});

        panel.add(new JScrollPane(currentVehiclesTable), BorderLayout.CENTER);
        return panel;
    }

    // --- SECTION 4: FINANCIAL TAB
    private JPanel createFinancialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
    
        // A. Revenue Summary
        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue Summary"));
        lblTotalRevenue = new JLabel("Total Revenue Collected: RM 1,250.00");
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalRevenue.setForeground(new Color(0, 150, 0));
        revenuePanel.add(lblTotalRevenue);
    
        JPanel finesSection = new JPanel(new BorderLayout(5, 5));
        finesSection.setBorder(BorderFactory.createTitledBorder("Outstanding Unpaid Fines"));

        // Action Bar (Manage All Fines button)
        JPanel actionBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnManageAll = new JButton("Manage All Fines");
        btnManageAll.addActionListener(e -> {
            JFrame fineFrame = new JFrame("Fine Management System");
            fineFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            fineFrame.setSize(900, 600);
            fineFrame.add(new FinePanel());
            fineFrame.setLocationRelativeTo(this);
            fineFrame.setVisible(true);
        });
        actionBar.add(btnManageAll);
        finesSection.add(actionBar, BorderLayout.NORTH);

        // --- LINKING TO DATABASE ---
        String[] columns = {"Fine ID", "Plate", "Amount (RM)", "Reason", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        finesTable = new JTable(model);

        // Fetch live data from FineManager [cite: 2026-02-13]
        java.util.List<model.Fine> allFines = FineManager.view_all_fines();
        if (allFines != null) {
            for (model.Fine f : allFines) {
                // Only show UNPAID fines in this specific dashboard table [cite: 2026-02-14]
                if (!f.isPaid()) {
                    model.addRow(new Object[]{
                        f.getFineID(),
                        f.getVehiclePlate(),
                        String.format("%.2f", f.getAmount()),
                        f.getReason(),
                        // Use your custom formatter for a professional look [cite: 2026-02-14]
                        DatabaseManager.formatDateTime(f.getIssueDate())
                    });
                }
            }
        }

        finesSection.add(new JScrollPane(finesTable), BorderLayout.CENTER);
        panel.add(finesSection, BorderLayout.CENTER);

        return panel;
    }

    // --- CONTROLLER ACTIONS ---
    private void handleUpdateScheme(ActionEvent e) {
        String selected = (String) fineSchemeCombo.getSelectedItem();
        JOptionPane.showMessageDialog(this, "Fine Scheme Updated to:\n" + selected);
        // TODO: Call FineManager.setScheme(selected) here later
    }
}