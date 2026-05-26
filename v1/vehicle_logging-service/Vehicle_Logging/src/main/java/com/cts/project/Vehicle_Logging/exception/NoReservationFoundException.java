package com.cts.project.Vehicle_Logging.exception;

public class NoReservationFoundException extends RuntimeException {
    public NoReservationFoundException(String vehicleNumber, Long slotId) {
        super("No active reservation found for vehicle " + vehicleNumber
                + " on slot " + slotId + ". Please make a reservation before entry.");
    }
}