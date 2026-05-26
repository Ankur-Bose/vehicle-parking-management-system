package com.cts.project.Vehicle_Logging.entity;

import com.cts.project.Vehicle_Logging.enums.LogStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "vehicle_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String vehicleNumber;

    @Column(nullable = false)
    private Long slotId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = true)
    private Long reservationId;

    @Column(nullable = false)
    private LocalDateTime entryTime;

    private LocalDateTime exitTime;

    private Long durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LogStatus status;
}