package com.cts.project.Vehicle_Logging.client;

import com.cts.project.Vehicle_Logging.dto.SlotResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class ParkingSlotClientFallback implements ParkingSlotClient {

    @Override
    public SlotResponseDTO getSlotById(Long id, String authHeader) {
        log.error("FALLBACK: slot-service is DOWN. Cannot fetch slot id={}", id);

        SlotResponseDTO fallback = new SlotResponseDTO();
        fallback.setId(id);
        fallback.setOccupiedStatus(1);   // treat as occupied = safe default
        fallback.setType("UNKNOWN");
        fallback.setLocation("UNKNOWN");
        return fallback;
    }

    @Override
    public SlotResponseDTO updateSlotStatus(Long id, Integer status, String authHeader) {
        log.error("FALLBACK: slot-service is DOWN. Cannot update slot id={} to status={}", id, status);
        // Return a dummy DTO — caller must handle this gracefully
        SlotResponseDTO fallback = new SlotResponseDTO();
        fallback.setId(id);
        fallback.setOccupiedStatus(status);
        fallback.setType("UNKNOWN");
        fallback.setLocation("UNKNOWN");
        return fallback;
    }
}