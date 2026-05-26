package com.cts.project.Vehicle_Logging.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class EntryRequest {

    @NotBlank(message = "Vehicle number is required")
    @Pattern(
            regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$",
            message = "Invalid vehicle number format. Example: TN09AB1234"
    )
    private String vehicleNumber;

    /** Slot id — required for walk-in, ignored for reserved (slot taken from reservation). */
    private Long slotId;

    @NotNull(message = "User ID is required")
    private Long userId;

    /**
     * Optional: ID of the active reservation for this vehicle.
     * When present  → PATH A (reserved): slot is taken from the reservation record.
     * When absent   → PATH B (walk-in):  slotId in this request must be free (-1).
     */
    private Long reservationId;
}
