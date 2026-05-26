package com.cts.project.reservationService.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDateTime;

// What the CLIENT sends to us
// Only the fields client needs to provide — nothing else
@Data
public class ReservationRequestDTO {
    @Size(min=4,max = 20,message = "Vehicle Number must be between 4 and 20 characters")
    @Pattern(regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$",message = "Invalid Vehicle Format")
    private String vehicleNumber;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}