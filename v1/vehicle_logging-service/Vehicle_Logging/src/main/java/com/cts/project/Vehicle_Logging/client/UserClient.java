package com.cts.project.Vehicle_Logging.client;


import com.cts.project.Vehicle_Logging.dto.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;


@FeignClient(
        name = "USER-SERVICE",
        fallback = UserClientFallback.class
)
public interface UserClient {

    @GetMapping("/users/internal/{id}")
    UserResponse getUserById(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader
    );
}
