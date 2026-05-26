package com.cts.project.reservationService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class SlotNotFoundException extends RuntimeException {
    public SlotNotFoundException(Long slotId) {
        super("Parking slot not found with id: " + slotId);
    }
}