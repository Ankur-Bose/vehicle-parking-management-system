package com.cts.project.reservationService.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParkingResponse {
    private Long id;
    private String type;
    private Integer occupiedStatus;
    private String location;
}