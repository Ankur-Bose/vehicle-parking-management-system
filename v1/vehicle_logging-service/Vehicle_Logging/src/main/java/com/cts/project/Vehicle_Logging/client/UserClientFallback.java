package com.cts.project.Vehicle_Logging.client;

import com.cts.project.Vehicle_Logging.dto.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class UserClientFallback implements UserClient {

    @Override
    public UserResponse getUserById(Long id, String authHeader) {
        log.error("FALLBACK: user-service is DOWN. Cannot validate user id={}", id);
        // Return a dummy UserResponse with a clear indicator.
        // The service layer checks for this and throws an appropriate exception.
        UserResponse fallback = new UserResponse();
        fallback.setId(-1L);   // sentinel value — service layer checks this
        fallback.setName("UNAVAILABLE");
        fallback.setEmail("UNAVAILABLE");
        fallback.setRole("UNAVAILABLE");
        return fallback;
    }
}