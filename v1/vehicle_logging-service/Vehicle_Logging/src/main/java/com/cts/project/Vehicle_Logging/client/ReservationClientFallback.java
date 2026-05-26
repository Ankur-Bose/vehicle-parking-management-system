package com.cts.project.Vehicle_Logging.client;

import com.cts.project.Vehicle_Logging.dto.ReservationResponse;
import com.cts.project.Vehicle_Logging.dto.ReservationUpdateRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class ReservationClientFallback implements ReservationClient {

    @Override
    public List<ReservationResponse> getActiveReservations() {
        log.error("FALLBACK: reservation-service DOWN.");
        return Collections.emptyList();
    }

    @Override
    public ReservationResponse getReservationById(Long id) {
        log.error("FALLBACK: reservation-service DOWN. Cannot fetch id={}", id);
        ReservationResponse fallback = new ReservationResponse();
        fallback.setId(-1L);
        fallback.setStatus("UNAVAILABLE");
        return fallback;
    }

    @Override
    public ReservationResponse updateReservation(Long id, ReservationUpdateRequest request) {
        log.error("FALLBACK: reservation-service DOWN. Cannot update id={}", id);
        ReservationResponse fallback = new ReservationResponse();
        fallback.setId(-1L);
        fallback.setStatus("UNAVAILABLE");
        return fallback;
    }
}