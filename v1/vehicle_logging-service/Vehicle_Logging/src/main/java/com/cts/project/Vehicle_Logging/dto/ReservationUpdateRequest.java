package com.cts.project.Vehicle_Logging.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReservationUpdateRequest {
    private String vehicleNumber;
    private LocalDateTime startTime;

    private LocalDateTime endTime;
    private String status;
}
