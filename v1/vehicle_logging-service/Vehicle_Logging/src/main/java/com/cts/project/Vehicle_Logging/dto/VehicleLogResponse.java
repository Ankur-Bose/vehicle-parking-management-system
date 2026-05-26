package com.cts.project.Vehicle_Logging.dto;

import com.cts.project.Vehicle_Logging.enums.LogStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "Vehicle log entry returned by the API")
public class VehicleLogResponse {

    @Schema(description = "Unique log ID", example = "1")
    private Long logId;

    @Schema(description = "Vehicle registration number", example = "TN09AB1234")
    private String vehicleNumber;

    @Schema(description = "Parking slot ID", example = "1")
    private Long slotId;

    @Schema(description = "User ID who logged the entry", example = "1")
    private Long userId;

    @Schema(description = "Linked reservation ID — null if walk-in", example = "123456")
    private Long reservationId;

    @Schema(description = "Entry timestamp")
    private LocalDateTime entryTime;

    @Schema(description = "Exit timestamp — null while vehicle is parked")
    private LocalDateTime exitTime;

    @Schema(description = "Duration parked in minutes — null while active", example = "45")
    private Long durationMinutes;

    @Schema(description = "ACTIVE while parked, COMPLETED after exit")
    private LogStatus status;
}