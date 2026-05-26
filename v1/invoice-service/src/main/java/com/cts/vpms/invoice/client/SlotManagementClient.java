package com.cts.vpms.invoice.client;

import com.cts.vpms.invoice.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "slotManagement-service", configuration = FeignClientConfig.class)
public interface SlotManagementClient {

    @GetMapping("/slots/internal/{slotId}")
    SlotManagementResponse getSlotById(@PathVariable("slotId") Long slotId);
}