package com.cts.project.reservationService.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class UserClientFallback implements UserClient {

    private static final Logger log = LoggerFactory.getLogger(UserClientFallback.class);

    @Override
    public UserResponse getUserById(long id) {
        log.error("Circuit breaker triggered: USER-SERVICE is down. getUserById({})", id);
        return new UserResponse(id, "UNKNOWN", "unavailable@service.com", "N/A", "UNKNOWN");
    }
}