package com.cts.project.reservationService.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "USER-SERVICE", fallback = UserClientFallback.class)
public interface UserClient {

    @GetMapping("/users/internal/{id}")  // ✅ correct path
    UserResponse getUserById(@PathVariable long id);
}