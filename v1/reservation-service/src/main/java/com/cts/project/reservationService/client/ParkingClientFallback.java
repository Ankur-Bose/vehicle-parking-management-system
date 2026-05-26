package com.cts.project.reservationService.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ParkingClientFallback implements ParkingClient {

    private static final Logger log = LoggerFactory.getLogger(ParkingClientFallback.class);

    @Override
    public ParkingResponse getSlotById(Long id) {
        log.error("Circuit breaker triggered: slotManagement-service is down. getSlotById({})", id);
        return new ParkingResponse(id, "UNKNOWN", -1, "SERVICE_UNAVAILABLE");
    }

    @Override
    public ParkingResponse updateSlotStatus(Long id, int status) {
        log.error("Circuit breaker triggered: slotManagement-service is down. updateSlotStatus({}, {})", id, status);
        return new ParkingResponse(id, "UNKNOWN", -1, "SERVICE_UNAVAILABLE");
    }
}
