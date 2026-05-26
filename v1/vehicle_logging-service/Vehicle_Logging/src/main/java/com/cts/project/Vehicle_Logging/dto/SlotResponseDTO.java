package com.cts.project.Vehicle_Logging.dto;


import lombok.Data;

@Data
public class SlotResponseDTO {
    private Long id;
    private Integer occupiedStatus;  // -1=available, 0=available(reserved), 1=occupied
    private String location;
    private String type;
}