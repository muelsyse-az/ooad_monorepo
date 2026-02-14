package ui;

import javax.swing.*;
import java.awt.*;
import model.Fine;
import model.FineManager;

public class IssueFineDialog extends JDialog {
    private JTextField txtPlate = new JTextField(15);
    private JTextField txtAmount = new JTextField(10);
    private JTextField txtDuration = new JTextField(10);
    private JComboBox<String> comboReason;
    private JComboBox<String> comboScheme;
    private JPanel amountRow;
    private JPanel overstayPanel; // Container for duration and scheme
    private boolean succeeded = false;

    public IssueFineDialog(Frame parent) {
        super(parent, "Issue New Fine", true);
        setLayout(new BorderLayout(10, 10));
        
        JPanel mainForm = new JPanel();
        mainForm.setLayout(new BoxLayout(mainForm, BoxLayout.Y_AXIS));
        mainForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Vehicle Plate (Now First) [cite: 2026-02-14]
        mainForm.add(createFieldRow("Vehicle Plate:", txtPlate));
        mainForm.add(Box.createVerticalStrut(10));

        // 2. Reason Selection [cite: 2026-02-14]
        JPanel reasonRow = new JPanel(new GridLayout(1, 2, 10, 10));
        reasonRow.add(new JLabel("Reason:"));
        String[] reasons = {"Illegal Parking", "Overstayed", "Obstruction", "No Permit"};
        comboReason = new JComboBox<>(reasons);
        comboReason.addActionListener(e -> toggleFormLayout());
        reasonRow.add(comboReason);
        mainForm.add(reasonRow);
        mainForm.add(Box.createVerticalStrut(10));

        // 3. Manual Amount Row (Hidden if Overstayed)[ite: 2026-02-14]c
        amountRow = createFieldRow("Fine Amount (RM):", txtAmount);
        mainForm.add(amountRow);

        // 4. Overstay Configuration Panel (Hidden unless Overstayed) [cite: 2026-02-14]
        overstayPanel = new JPanel();
        overstayPanel.setLayout(new BoxLayout(overstayPanel, BoxLayout.Y_AXIS));
        
        overstayPanel.add(createFieldRow("Overstay Duration (Hrs):", txtDuration));
        overstayPanel.add(Box.createVerticalStrut(10));
        
        String[] schemes = {"Option A: Fixed (RM 50)", "Option B: Progressive", "Option C: Hourly"};
        comboScheme = new JComboBox<>(schemes);
        overstayPanel.add(createFieldRow("Choose Scheme:", comboScheme));
        
        overstayPanel.setVisible(false); // Default hidden
        mainForm.add(overstayPanel);

        add(mainForm, BorderLayout.CENTER);

        // Buttons
        JButton btnConfirm = new JButton("Confirm & Issue");
        btnConfirm.addActionListener(e -> handleIssue());
        JPanel btnPanel = new JPanel();
        btnPanel.add(btnConfirm);
        add(btnPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    private void toggleFormLayout() {
        boolean isOverstayed = "Overstayed".equals(comboReason.getSelectedItem());
        
        // Toggle visibility [cite: 2026-02-14]
        amountRow.setVisible(!isOverstayed);
        overstayPanel.setVisible(isOverstayed);
        
        pack(); // Resizes window to fit contents
        revalidate();
        repaint();
    }

    private JPanel createFieldRow(String label, JComponent field) {
        JPanel row = new JPanel(new GridLayout(1, 2, 10, 10));
        row.add(new JLabel(label));
        row.add(field);
        return row;
    }

    private void handleIssue() {
        try {
            String plate = txtPlate.getText().trim().toUpperCase();
            String reason = (String) comboReason.getSelectedItem();
            double amount;
            String scheme;
    
            if (plate.isEmpty()) throw new Exception("Plate number is required.");
    
            if ("Overstayed".equals(reason)) {
                // Logic for Overstayed [cite: 2026-02-14]
                if (txtDuration.getText().isEmpty()) throw new Exception("Duration is required.");
                
                double hours = Double.parseDouble(txtDuration.getText().trim());
                String selectedScheme = (String) comboScheme.getSelectedItem();
                
                // Map the JComboBox text to your method's expected keys [cite: 2026-02-14]
                if (selectedScheme.contains("Fixed")) scheme = "Fixed";
                else if (selectedScheme.contains("Progressive")) scheme = "Progressive";
                else scheme = "Hourly";
    
                // CALL YOUR NEW METHOD
                amount = FineManager.calculate_fine_amount(scheme, hours);
                reason += " (" + hours + " hrs)";
            } else {
                // Logic for Manual Amount [cite: 2026-02-14]
                if (txtAmount.getText().isEmpty()) throw new Exception("Amount is required.");
                amount = Double.parseDouble(txtAmount.getText().trim());
                scheme = "Fixed";
            }
    
            Fine newFine = Fine.create_new(plate, amount, reason, scheme); 
            
            if (FineManager.save_fine(newFine)) {
                succeeded = true;
                dispose();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for amount/duration.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
    public boolean isSucceeded() { return succeeded; }
}