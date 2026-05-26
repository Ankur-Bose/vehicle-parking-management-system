package com.cts.project.reservationService.repository;

import com.cts.project.reservationService.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT COUNT(r) > 0 FROM Reservation r WHERE r.slotId = :slotId " +
            "AND r.status = 'CONFIRMED' " +
            "AND r.startTime < :newEndTime " +
            "AND (r.endTime IS NULL OR r.endTime > :newStartTime)")
    boolean existsOverlappingReservation(
            @Param("slotId") Long slotId,
            @Param("newStartTime") LocalDateTime newStartTime,
            @Param("newEndTime") LocalDateTime newEndTime);

    List<Reservation> findByUserId(Long userId);
    List<Reservation> findBySlotId(Long slotId);
    List<Reservation> findByStatus(String status);
}