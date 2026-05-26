package com.cts.vpms.invoice.client;

import com.cts.vpms.invoice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "vehicleLog-service", configuration = FeignClientConfig.class)
public interface VehicleLogClient {
    @GetMapping("/logs/internal/{id}")
    VehicleLogResponse getLogById(@PathVariable Long id);
}