package com.cts.project.reservationService.dto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import java.time.LocalDateTime;

// What WE send back to the client
// Clean, controlled — no passwordHash, no unnecessary fields
@Data
@JsonPropertyOrder({
        "id",
        "status",
        "vehicleNumber",
        "startTime",
        "endTime",
        "userId",
        "userName",
        "slotId",
        "slotType",
        "slotLocation"
})
public class ReservationResponseDTO {

    private Long id;
    private String vehicleNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    // From User — only name, no passwordHash, no role exposed
    private Long userId;
    private String userName;

    // From ParkingSlot — only what client needs
    private Long slotId;
    private String slotType;
    private String slotLocation;
}