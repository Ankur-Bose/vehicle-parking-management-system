package com.cts.vpms.invoice.client;

import com.cts.vpms.invoice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "reservation-service", configuration = FeignClientConfig.class)
public interface ReservationClient {

    @GetMapping("/reservations/internal/{id}")
    ReservationResponse getReservationById(@PathVariable("id") Long id);
}