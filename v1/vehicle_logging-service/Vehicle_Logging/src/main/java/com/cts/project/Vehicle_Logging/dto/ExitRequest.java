package com.cts.project.Vehicle_Logging.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ExitRequest {

    @NotBlank(message = "Vehicle number is required")
    @Pattern(
            regexp = "^[A-Z]{2}[0-9]{2}[A-Z]{1,2}[0-9]{4}$",
            message = "Invalid vehicle number format. Example: TN09AB1234"
    )
    private String vehicleNumber;
}