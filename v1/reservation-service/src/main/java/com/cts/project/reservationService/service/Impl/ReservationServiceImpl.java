package com.cts.project.reservationService.service.Impl;

import com.cts.project.reservationService.client.ParkingClient;
import com.cts.project.reservationService.client.ParkingResponse;
import com.cts.project.reservationService.client.UserClient;
import com.cts.project.reservationService.client.UserResponse;
import com.cts.project.reservationService.entity.Reservation;
import com.cts.project.reservationService.exception.ReservationNotFoundException;
import com.cts.project.reservationService.exception.SlotAlreadyBookedException;
import com.cts.project.reservationService.exception.SlotNotFoundException;
import com.cts.project.reservationService.exception.UserException;
import com.cts.project.reservationService.repository.ReservationRepository;
import com.cts.project.reservationService.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private static final String STATUS_CANCELLED   = "CANCELLED";
    private static final String STATUS_CONFIRMED   = "CONFIRMED";
    private static final String STATUS_COMPLETED   = "COMPLETED";
    private static final String RES_NOT_FOUND_LOG  = "Reservation not found with id={}";
    private static final String USER_NOT_FOUND_LOG = "User not found with id={}";

    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ParkingClient parkingClient;
    @Autowired
    private UserClient userClient;

    @Override
    public Reservation createReservation(Long userId, Long slotId, Reservation reservation) {
        log.info("Creating reservation for userId={} slotId={}", userId, slotId);
        // Validate user exists via Feign
        UserResponse user;
        try {
            user = userClient.getUserById(userId);
        } catch (Exception e) {
            log.error(USER_NOT_FOUND_LOG, userId);
            throw new UserException(userId);
        }
        // Validate slot exists via Feign
        ParkingResponse slot;
        try {
            slot = parkingClient.getSlotById(slotId);
        } catch (Exception e) {
            log.error("Slot not found with id={}", slotId);
            throw new SlotNotFoundException(slotId);
        }
        // If no endTime provided, use far-future sentinel to check overlap
        LocalDateTime effectiveEndTime = reservation.getEndTime() != null
                ? reservation.getEndTime()
                : LocalDateTime.of(2038, 1, 1, 0, 0, 0);

        boolean alreadyBooked = reservationRepository.existsOverlappingReservation(
                slotId, reservation.getStartTime(), effectiveEndTime);
        if (alreadyBooked) {
            // Before rejecting, verify the slot's actual physical status.
            // If the slot is physically AVAILABLE (-1), the CONFIRMED reservation(s)
            // are stale — the vehicle already left but the reservation was never
            // marked COMPLETED (e.g. the service was down during vehicle exit).
            // Auto-complete those stale reservations and allow the new booking.
            boolean slotPhysicallyFree = false;
            try {
                ParkingResponse currentSlot = parkingClient.getSlotById(slotId);
                slotPhysicallyFree = (currentSlot != null && Integer.valueOf(-1).equals(currentSlot.getOccupiedStatus()));
            } catch (Exception e) {
                log.warn("Could not fetch slot {} status during overlap check. Reason: {}", slotId, e.getMessage());
            }

            if (slotPhysicallyFree) {
                // Auto-complete all stale CONFIRMED reservations for this slot
                log.info("Slot {} is physically available but has stale CONFIRMED reservation(s). Auto-completing them.", slotId);
                reservationRepository.findBySlotId(slotId).stream()
                        .filter(r -> STATUS_CONFIRMED.equals(r.getStatus()))
                        .forEach(stale -> {
                            stale.setStatus(STATUS_COMPLETED);
                            if (stale.getEndTime() == null) {
                                stale.setEndTime(LocalDateTime.now());
                            }
                            reservationRepository.save(stale);
                            log.info("Auto-completed stale reservation id={} for slot={}", stale.getId(), slotId);
                        });
            } else {
                log.warn("Slot {} already booked for requested time", slotId);
                throw new SlotAlreadyBookedException(slotId);
            }
        }
        // Mark slot as reserved (0) — becomes occupied (1) only when vehicle physically enters
        parkingClient.updateSlotStatus(slotId, 0);
        reservation.setUserId(userId);
        reservation.setSlotId(slotId);
        reservation.setStatus(STATUS_CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);
        log.info("Reservation created id={}", saved.getId());
        return saved;
    }
    //getting reservation data by Id
    @Override
    public Reservation getReservationById(Long id) {
        log.info("Fetching reservation id={}", id);
        return reservationRepository.findById(id)
                .orElseThrow(() -> {
                    log.error(RES_NOT_FOUND_LOG, id);
                    return new ReservationNotFoundException(id);
                });
    }
    //getting all the reservations
    @Override
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }
    //getting only cancelled reservations
    @Override
    public List<Reservation> getAllCancelledReservations() {
        return reservationRepository.findByStatus(STATUS_CANCELLED);
    }
    //getting all user reservations so far by their Id
    @Override
    public List<Reservation> getAllReservationsByUserId(Long userId) {
        try {
            userClient.getUserById(userId);
        } catch (Exception e) {
            log.error(USER_NOT_FOUND_LOG, userId);
            throw new UserException(userId);
        }
        return reservationRepository.findByUserId(userId);
    }
    //getting active reservations not completed or cancelled
    @Override
    public List<Reservation> getActiveReservations() {
        return reservationRepository.findByStatus(STATUS_CONFIRMED);
    }
    //cancelling the reservations
    @Override
    public Reservation cancelReservation(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        if (STATUS_CANCELLED.equals(reservation.getStatus())) {
            throw new ReservationNotFoundException("Reservation is already cancelled");
        }
        // Free the slot via Feign
        parkingClient.updateSlotStatus(reservation.getSlotId(), -1);
        reservation.setStatus(STATUS_CANCELLED);
        return reservationRepository.save(reservation);
    }
    //updating the info of the vehicle data..in reservation table
    @Override
    public Reservation updateReservation(Long id, Reservation updatedData) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));

        if (updatedData.getVehicleNumber() != null) {
            existing.setVehicleNumber(updatedData.getVehicleNumber());
        }
        if (updatedData.getStartTime() != null) {
            existing.setStartTime(updatedData.getStartTime());
        }
        // Setting endTime means the session is done — mark complete and free the slot
        if (updatedData.getEndTime() != null) {
            existing.setEndTime(updatedData.getEndTime());
            existing.setStatus(STATUS_COMPLETED);
            // Wrap slot-free in try-catch: vehicle-logging-service may have already freed it.
            // A circuit-breaker CallNotPermittedException must NOT roll back the @Transactional.
            try {
                parkingClient.updateSlotStatus(existing.getSlotId(), -1);
            } catch (Exception e) {
                log.warn("Could not free slot {} during reservation completion (may already be freed). Reason: {}",
                        existing.getSlotId(), e.getMessage());
            }
        }
        if (updatedData.getStatus() != null && updatedData.getEndTime() == null) {
            existing.setStatus(updatedData.getStatus());
        }
        return reservationRepository.save(existing);
    }
}