package com.cts.project.Vehicle_Logging.exception;

public class SlotNotAvailableException extends RuntimeException {

    public SlotNotAvailableException(Long slotId) {
        super("Parking slot " + slotId + " is not available.");
    }

    public SlotNotAvailableException(Long slotId, String reason) {
        super(reason);
    }
}
