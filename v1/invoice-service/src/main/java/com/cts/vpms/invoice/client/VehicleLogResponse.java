package com.cts.vpms.invoice.client;

import java.time.LocalDateTime;

public class VehicleLogResponse {
    private Long logId;
    private String vehicleNumber;
    private Long slotId;
    private Long userId;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private Long durationMinutes;
    private String status;          // ← String instead of LogStatus enum (different module)

    public Long getLogId() { return logId; }
    public String getVehicleNumber() { return vehicleNumber; }
    public Long getSlotId() { return slotId; }
    public Long getUserId() { return userId; }
    public LocalDateTime getEntryTime() { return entryTime; }
    public LocalDateTime getExitTime() { return exitTime; }
    public Long getDurationMinutes() { return durationMinutes; }
    public String getStatus() { return status; }
}