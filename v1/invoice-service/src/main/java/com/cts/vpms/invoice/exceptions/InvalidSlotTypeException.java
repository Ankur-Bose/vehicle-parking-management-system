package com.cts.vpms.invoice.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidSlotTypeException extends BillingException {
    public static InvalidSlotTypeException forValue(String value) {
        return new InvalidSlotTypeException(
                "Invalid slot type: '" + value + "'. Accepted: TWO_WHEELER, FOUR_WHEELER, 2W, 4W"
        );
    }
    public InvalidSlotTypeException(String message) {
        super(message, HttpStatus.BAD_REQUEST, "INVALID_SLOT_TYPE");
    }
}