package ui;

import db.DatabaseManager;
import model.PaymentMethod;
import model.Receipt;
import service.PaymentService;

import javax.swing.*;
import java.awt.*;

public class PaymentPanel extends JPanel {
    private final PaymentService paymentService;

    private final JTextField txtPlate = new JTextField(12);
    private final JTextField txtTotalDue = new JTextField(8);
    private final JTextField txtCash = new JTextField(8);
    private final JTextArea txtReceipt = new JTextArea(12, 30);

    private final JRadioButton rbCash = new JRadioButton("Cash", true);
    private final JRadioButton rbCard = new JRadioButton("Card");
    private final JCheckBox cbPayFines = new JCheckBox("Mark unpaid fines as paid");

    public PaymentPanel(DatabaseManager db) {
        this.paymentService = new PaymentService(db);
        buildUI();
    }

    public void setBill(String plate, double totalDue) {
        txtPlate.setText(plate);
        txtTotalDue.setText(String.format("%.2f", totalDue));
    }

    private void buildUI() {
        setLayout(new BorderLayout(10,10));
        setBorder(BorderFactory.createEmptyBorder(12,12,12,12));

        JPanel top = new JPanel(new GridLayout(5,2,8,8));

        txtTotalDue.setEditable(false);
        ButtonGroup bg = new ButtonGroup();
        bg.add(rbCash); bg.add(rbCard);

        top.add(new JLabel("Plate No:"));
        top.add(txtPlate);

        top.add(new JLabel("Total Due (RM):"));
        top.add(txtTotalDue);

        top.add(new JLabel("Method:"));
        JPanel m = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        m.add(rbCash); m.add(rbCard);
        top.add(m);

        top.add(new JLabel("Cash Amount (RM):"));
        top.add(txtCash);

        top.add(new JLabel(""));
        top.add(cbPayFines);

        add(top, BorderLayout.NORTH);

        JButton btnPay = new JButton("Pay");
        btnPay.addActionListener(e -> handlePay());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnPay);
        add(bottom, BorderLayout.SOUTH);

        txtReceipt.setEditable(false);
        txtReceipt.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        add(new JScrollPane(txtReceipt), BorderLayout.CENTER);

        rbCard.addActionListener(e -> txtCash.setEnabled(false));
        rbCash.addActionListener(e -> txtCash.setEnabled(true));
    }

    private void handlePay() {
        try {
            String plate = txtPlate.getText().trim();
            double totalDue = Double.parseDouble(txtTotalDue.getText().trim());

            PaymentMethod method = rbCash.isSelected() ? PaymentMethod.CASH : PaymentMethod.CARD;
            double amountPaid = (method == PaymentMethod.CASH) ? Double.parseDouble(txtCash.getText().trim()) : totalDue;

            Receipt r = paymentService.processPayment(plate, totalDue, method, amountPaid, cbPayFines.isSelected());
            txtReceipt.setText(r.getReceiptText());

            JOptionPane.showMessageDialog(this, "Payment Successful!");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Payment Failed", JOptionPane.ERROR_MESSAGE);
        }
    }
}
