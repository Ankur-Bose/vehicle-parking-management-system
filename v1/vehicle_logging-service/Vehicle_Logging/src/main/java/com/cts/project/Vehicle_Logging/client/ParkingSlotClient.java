package com.cts.project.Vehicle_Logging.client;



import com.cts.project.Vehicle_Logging.dto.SlotResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "SLOTMANAGEMENT-SERVICE",
        fallback = ParkingSlotClientFallback.class
)
public interface ParkingSlotClient {

    @GetMapping("/slots/{id}")
    SlotResponseDTO getSlotById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader
    );

    @PatchMapping("/slots/{id}")
    SlotResponseDTO updateSlotStatus(
            @PathVariable("id") Long id,
            @RequestParam("status") Integer status,
            @RequestHeader("Authorization") String authHeader
    );
}

