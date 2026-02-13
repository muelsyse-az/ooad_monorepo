package model;

public class ParkingSpot {

    // Define the specific types and their base hourly rates
    public enum SpotType {
        COMPACT(2.00),      // [cite: 42]
        REGULAR(5.00),      // [cite: 42]
        HANDICAPPED(2.00),  // [cite: 43]
        RESERVED(10.00);    // [cite: 44]

        public final double rate;

        SpotType(double rate) {
            this.rate = rate;
        }
    }

    private String spotID;      // e.g., "F1-R1-S1"
    private int floor;
    private int row;
    private SpotType type;
    private boolean isOccupied;
    private String currentVehiclePlate; // Null if empty

    // Constructor
    public ParkingSpot(String spotID, int floor, int row, SpotType type, boolean isOccupied, String currentVehiclePlate) {
        this.spotID = spotID;
        this.floor = floor;
        this.row = row;
        this.type = type;
        this.isOccupied = isOccupied;
        this.currentVehiclePlate = currentVehiclePlate;
    }

    // --- Business Logic ---

    // [cite: 81] Calculate fee based on spot type and duration
    public double calculateBaseFee(double hours) {
        return hours * this.type.rate;
    }

    public void parkVehicle(String plate) {
        this.isOccupied = true;
        this.currentVehiclePlate = plate;
    }

    public void removeVehicle() {
        this.isOccupied = false;
        this.currentVehiclePlate = null;
    }

    // --- Getters ---
    public String getSpotID() { return spotID; }
    public int getFloor() { return floor; }
    public int getRow() { return row; }
    public SpotType getType() { return type; }
    public boolean isOccupied() { return isOccupied; }
    public String getCurrentVehiclePlate() { return currentVehiclePlate; }

    @Override
    public String toString() {
        return String.format("[%s] Floor %d | Type: %s | Status: %s",
                spotID, floor, type, (isOccupied ? "OCCUPIED by " + currentVehiclePlate : "AVAILABLE"));
    }
}