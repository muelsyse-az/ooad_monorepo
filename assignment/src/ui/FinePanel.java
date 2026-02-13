package ui;

import com.formdev.flatlaf.FlatDarkLaf;

import javax.security.auth.RefreshFailedException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import model.FineManager;
import model.Fine;

public class FinePanel extends JPanel {
    private JTextField txtPlate;
    private JTextArea txtDetails;
    private DefaultTableModel tableModel;
    private JTable fineTable;             

    public FinePanel() {

        // Use a BorderLayout for organized structure
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- TOP SECTION: SEARCH ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Vehicle Plate:"));
        txtPlate = new JTextField(15);
        JButton btnSearch = new JButton("Search Fine");
        JButton btnRefresh = new JButton("Refresh Table");
        
        searchPanel.add(txtPlate);
        searchPanel.add(btnSearch);
        add(searchPanel, BorderLayout.NORTH);

        // --- CENTER SECTION: DISPLAY ---
        String[] columns = {"ID", "Plate", "Amount", "Reason", "Status", "Date"};
        tableModel = new DefaultTableModel(columns, 0);
        fineTable = new JTable(tableModel);
        
        txtDetails = new JTextArea(5, 20);
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, 
                new JScrollPane(txtDetails), 
                new JScrollPane(fineTable));
        splitPane.setDividerLocation(120);
        add(splitPane, BorderLayout.CENTER);

        // --- BOTTOM SECTION: ACTIONS ---
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnPay = new JButton("Process Payment");
        actionPanel.add(btnPay);
        add(actionPanel, BorderLayout.SOUTH);

        // --- LOGIC ---
        btnSearch.addActionListener(e -> performSearch());
        btnPay.addActionListener(e -> performPayment());
        btnRefresh.addActionListener(e -> refreshTableData());

        refreshTableData();
    }

    private void performSearch() {
        String plate = txtPlate.getText().trim();
        if (plate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a plate number.");
            return;
        }

        Fine fine = FineManager.get_fine(plate, false); // Search for unpaid fine
        if (fine != null) {
            txtDetails.setText(
                "--- FINE DETAILS ---\n" +
                "ID:     " + fine.getFineID() + "\n" +
                "Amount: RM " + fine.getAmount() + "\n" +
                "Reason: " + fine.getReason() + "\n" +
                "Date:   " + fine.getIssueDate()
            );
        } else {
            txtDetails.setText("No unpaid fines found for: " + plate);
        }
    }

    private void performPayment() {
        String plate = txtPlate.getText().trim();
        // Trigger your payment logic and refresh display
        FineManager.process_payment(plate, "Cash"); 
        JOptionPane.showMessageDialog(this, "Payment successful for " + plate);
        txtDetails.setText("Payment completed.");
        refreshTableData();
    }

    public void refreshTableData() {
        tableModel.setRowCount(0); 
        
        // Call your existing logic!
        List<Fine> allFines = FineManager.view_all_fines(); 
    
        for (Fine f : allFines) {
            tableModel.addRow(new Object[]{
                f.getFineID(),
                f.getVehiclePlate(),
                "RM " + f.getAmount(),
                f.getReason(),
                f.isPaid() ? "PAID" : "UNPAID",
                f.getIssueDate()
            });
        }
    }

    public static void main(String[] args) {
        FlatDarkLaf.setup(); // Apply your chosen theme
        JFrame frame = new JFrame("Fine Management Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new FinePanel()); // Add your custom panel
        frame.setSize(600, 400);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

