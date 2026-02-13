package model;

import java.time.LocalDateTime;

public class Payment {
    private String paymentId;
    private String plate;
    private double totalDue;
    private double amountPaid;
    private double changeAmount;
    private PaymentMethod method;
    private LocalDateTime paymentTime;

    public Payment(String paymentId, String plate, double totalDue,
                   double amountPaid, double changeAmount,
                   PaymentMethod method, LocalDateTime paymentTime) {
        this.paymentId = paymentId;
        this.plate = plate;
        this.totalDue = totalDue;
        this.amountPaid = amountPaid;
        this.changeAmount = changeAmount;
        this.method = method;
        this.paymentTime = paymentTime;
    }

    public String getPaymentId() { return paymentId; }
    public String getPlate() { return plate; }
    public double getTotalDue() { return totalDue; }
    public double getAmountPaid() { return amountPaid; }
    public double getChangeAmount() { return changeAmount; }
    public PaymentMethod getMethod() { return method; }
    public LocalDateTime getPaymentTime() { return paymentTime; }
}
