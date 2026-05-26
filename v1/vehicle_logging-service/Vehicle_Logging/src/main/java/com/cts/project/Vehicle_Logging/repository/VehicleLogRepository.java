package com.cts.project.Vehicle_Logging.repository;


import com.cts.project.Vehicle_Logging.entity.VehicleLog;
import com.cts.project.Vehicle_Logging.enums.LogStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VehicleLogRepository extends JpaRepository<VehicleLog, Long> {
    Optional<VehicleLog> findByVehicleNumberAndStatus(String vehicleNumber, LogStatus status);
    List<VehicleLog> findByVehicleNumber(String vehicleNumber);
    List<VehicleLog> findByStatus(LogStatus status);
    List<VehicleLog> findByUserId(Long userId);
    boolean existsBySlotIdAndStatus(Long slotId, LogStatus status);
}


