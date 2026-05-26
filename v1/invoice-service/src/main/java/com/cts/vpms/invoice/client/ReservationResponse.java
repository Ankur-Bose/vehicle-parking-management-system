package com.cts.vpms.invoice.client;

import java.time.LocalDateTime;

public class ReservationResponse {
    private Long id;
    private String vehicleNumber;
    private LocalDateTime startTime;

    // Getters
    public Long getId() { return id; }
    public String getVehicleNumber() { return vehicleNumber; }
    public LocalDateTime getStartTime() { return startTime; }
}