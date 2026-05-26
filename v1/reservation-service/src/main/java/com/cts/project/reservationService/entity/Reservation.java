package com.cts.project.reservationService.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {

    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(nullable = false, length = 15)
    private String status;

    @PrePersist
    public void generateId() {
        if (this.id == null) {
            this.id = ThreadLocalRandom.current().nextLong(100000L, 1000000L);
        }
    }
}