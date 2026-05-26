package com.cts.project.Vehicle_Logging.exception;

public class ActiveSessionAlreadyExistsException extends RuntimeException {
    public ActiveSessionAlreadyExistsException(String vehicleNumber) {
        super("Vehicle " + vehicleNumber + " already has an active parking session.");
    }
}
