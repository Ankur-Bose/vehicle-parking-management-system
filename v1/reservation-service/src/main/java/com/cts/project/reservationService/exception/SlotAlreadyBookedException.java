package com.cts.project.reservationService.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)    // 409 — slot exists but is taken
public class SlotAlreadyBookedException extends RuntimeException {
    public SlotAlreadyBookedException(Long slotId) {
        super("Slot " + slotId + " is already booked!");
    }
}