package model;

import java.time.LocalDateTime;

public class VehicleLog {
    private final String ticketID;
    private final String vehiclePlate;
    private final String spotID;
    private final String vehicleType;
    private final LocalDateTime entryTime;
    private final LocalDateTime exitTime;

    public VehicleLog(String ticketID, String vehiclePlate, String spotID,
                      String vehicleType, LocalDateTime entryTime, LocalDateTime exitTime) {
        this.ticketID = ticketID;
        this.vehiclePlate = vehiclePlate;
        this.spotID = spotID;
        this.vehicleType = vehicleType;
        this.entryTime = entryTime;
        this.exitTime = exitTime;
    }

    public String getTicketID() { return ticketID; }
    public String getVehiclePlate() { return vehiclePlate; }
    public String getSpotID() { return spotID; }
    public String getVehicleType() { return vehicleType; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
}
