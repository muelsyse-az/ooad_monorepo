package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

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

        // Fine Scheme Selector [cite: 448]
        panel.add(new JLabel("Current Fine Scheme:"));

        String[] schemes = {"Option A: Fixed (RM 50)", "Option B: Progressive", "Option C: Hourly"};
        fineSchemeCombo = new JComboBox<>(schemes);
        panel.add(fineSchemeCombo);

        JButton btnUpdate = new JButton("Apply New Scheme");
        btnUpdate.addActionListener(this::handleUpdateScheme); // Link to Button Click
        panel.add(btnUpdate);

        // Occupancy Metric [cite: 444]
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

    // --- SECTION 4: FINANCIAL TAB [cite: 445, 447] ---
    private JPanel createFinancialPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // A. Total Revenue Header
        JPanel revenuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        revenuePanel.setBorder(BorderFactory.createTitledBorder("Revenue Summary"));

        lblTotalRevenue = new JLabel("Total Revenue Collected: RM 1,250.00");
        lblTotalRevenue.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTotalRevenue.setForeground(new Color(0, 150, 0)); // Dark Green
        revenuePanel.add(lblTotalRevenue);

        panel.add(revenuePanel, BorderLayout.NORTH);

        // B. Unpaid Fines Table
        JPanel finesPanel = new JPanel(new BorderLayout());
        finesPanel.setBorder(BorderFactory.createTitledBorder("Outstanding Unpaid Fines"));

        String[] columns = {"Fine ID", "Plate", "Amount (RM)", "Reason", "Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        finesTable = new JTable(model);

        // STUB DATA
        model.addRow(new Object[]{"F-1002", "KUL3333", "50.00", "Overstayed > 24h", "2026-02-12"});
        model.addRow(new Object[]{"F-1005", "PEN5555", "100.00", "Illegal Parking", "2026-02-13"});

        finesPanel.add(new JScrollPane(finesTable), BorderLayout.CENTER);
        panel.add(finesPanel, BorderLayout.CENTER);

        return panel;
    }

    // --- CONTROLLER ACTIONS ---
    private void handleUpdateScheme(ActionEvent e) {
        String selected = (String) fineSchemeCombo.getSelectedItem();
        JOptionPane.showMessageDialog(this, "Fine Scheme Updated to:\n" + selected);
        // TODO: Call FineManager.setScheme(selected) here later
    }
}