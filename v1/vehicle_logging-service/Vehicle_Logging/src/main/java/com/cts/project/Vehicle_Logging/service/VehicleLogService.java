package com.cts.project.Vehicle_Logging.service;


import com.cts.project.Vehicle_Logging.dto.EntryRequest;
import com.cts.project.Vehicle_Logging.dto.ExitRequest;
import com.cts.project.Vehicle_Logging.dto.VehicleLogResponse;

import java.util.List;


public interface VehicleLogService {
    VehicleLogResponse logEntry(EntryRequest request, String authHeader);
    VehicleLogResponse logExit(ExitRequest request, String authHeader);
    VehicleLogResponse getLogById(Long logId);
    List<VehicleLogResponse> getLogsByVehicle(String vehicleNumber);
    List<VehicleLogResponse> getAllActiveLogs();
    List<VehicleLogResponse> getAllLogs();
    List<VehicleLogResponse> getLogsByUser(Long userId);
}
