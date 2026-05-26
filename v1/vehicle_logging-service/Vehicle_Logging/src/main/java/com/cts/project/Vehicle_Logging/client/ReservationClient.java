package com.cts.project.Vehicle_Logging.client;

import com.cts.project.Vehicle_Logging.dto.ReservationResponse;
import com.cts.project.Vehicle_Logging.dto.ReservationUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@FeignClient(
        name = "${feign.client.reservation-service}",
        fallback = ReservationClientFallback.class
)
public interface ReservationClient {

    @GetMapping("/reservations/active")
    List<ReservationResponse> getActiveReservations();

    @GetMapping("/reservations/internal/{id}")
    ReservationResponse getReservationById(@PathVariable("id") Long id);

    @PutMapping("/reservations/internal/{id}")
    ReservationResponse updateReservation(
            @PathVariable("id") Long id,
            @RequestBody ReservationUpdateRequest request
    );
}