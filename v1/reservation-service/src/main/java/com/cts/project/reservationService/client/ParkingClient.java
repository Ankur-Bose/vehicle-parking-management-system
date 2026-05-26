package com.cts.project.reservationService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "slotManagement-service", fallback = ParkingClientFallback.class)
public interface ParkingClient {

    @GetMapping("/slots/{id}")
    ParkingResponse getSlotById(@PathVariable Long id);

    @PatchMapping("/slots/{id}")
    ParkingResponse updateSlotStatus(
            @PathVariable Long id,
            @RequestParam("status") int status
    );
}