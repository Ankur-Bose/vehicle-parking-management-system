package com.cts.project.Vehicle_Logging.service.impl;

import com.cts.project.Vehicle_Logging.client.ParkingSlotClient;
import com.cts.project.Vehicle_Logging.client.ReservationClient;
import com.cts.project.Vehicle_Logging.client.UserClient;
import com.cts.project.Vehicle_Logging.dto.*;
import com.cts.project.Vehicle_Logging.entity.VehicleLog;
import com.cts.project.Vehicle_Logging.enums.LogStatus;
import com.cts.project.Vehicle_Logging.exception.ActiveSessionAlreadyExistsException;
import com.cts.project.Vehicle_Logging.exception.ServiceUnavailableException;
import com.cts.project.Vehicle_Logging.exception.SlotNotAvailableException;
import com.cts.project.Vehicle_Logging.exception.VehicleLogNotFoundException;
import com.cts.project.Vehicle_Logging.repository.VehicleLogRepository;
import com.cts.project.Vehicle_Logging.service.VehicleLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VehicleLogServiceImpl implements VehicleLogService {

    private final VehicleLogRepository vehicleLogRepository;
    private final ParkingSlotClient parkingSlotClient;
    private final UserClient userClient;
    private final ReservationClient reservationClient;

    // ── ENTRY

    @Override
    public VehicleLogResponse logEntry(EntryRequest request, String authHeader) {
        log.info("Entry request | vehicle={} slotId={} reservationId={} userId={}",
                request.getVehicleNumber(), request.getSlotId(),
                request.getReservationId(), request.getUserId());

        // 1. Check for duplicate active session
        Optional<VehicleLog> existing = vehicleLogRepository
                .findByVehicleNumberAndStatus(request.getVehicleNumber(), LogStatus.ACTIVE);
        if (existing.isPresent()) {
            log.warn("Duplicate active session | vehicle={}", request.getVehicleNumber());
            throw new ActiveSessionAlreadyExistsException(request.getVehicleNumber());
        }

        // 2. Validate user via Feign
        log.debug("Validating user id={}", request.getUserId());
        UserResponse user = userClient.getUserById(request.getUserId(), authHeader);
        if (user.getId() == -1L) {
            log.error("User-service unavailable | vehicle={}", request.getVehicleNumber());
            throw new ServiceUnavailableException(
                    "User service is currently unavailable. Please try again later.");
        }
        log.debug("User validated | name={}", user.getName());

        // 3. Determine PATH A (reserved) vs PATH B (walk-in)
        //
        //    PATH A — reservationId is present in the request → do a DIRECT single
        //             reservation lookup. This bypasses getActiveReservations() and
        //             is immune to circuit-breaker fallbacks returning empty lists.
        //
        //    PATH B — reservationId is absent → walk-in, slotId must be free (-1).

        Long resolvedSlotId       = null;
        Long matchedReservationId = null;
        boolean isReservedVehicle;

        if (request.getReservationId() != null) {
            // ── PATH A: RESERVED VEHICLE ──────────────────────────────────
            //
            // Strategy:
            //   1. Try direct reservation lookup to confirm slotId and vehicle match.
            //   2. If the reservation-service is down (fallback / circuit-breaker),
            //      trust the slotId the frontend already sent — it was populated from
            //      the same reservation record the staff selected.  This makes the
            //      gate robust against a temporarily unavailable reservation-service.
            //
            log.info("Reserved entry | reservationId={} vehicle={}",
                    request.getReservationId(), request.getVehicleNumber());

            boolean reservationVerified = false;

            try {
                ReservationResponse reservation =
                        reservationClient.getReservationById(request.getReservationId());

                if (reservation.getId() != -1L) {
                    // Reservation service is UP — validate vehicle ownership
                    if (!request.getVehicleNumber().equals(reservation.getVehicleNumber())) {
                        log.warn("Reservation vehicle mismatch | reservationId={} expected={} got={}",
                                request.getReservationId(), reservation.getVehicleNumber(),
                                request.getVehicleNumber());
                        throw new IllegalArgumentException(
                                "Reservation #" + request.getReservationId()
                                        + " does not belong to vehicle "
                                        + request.getVehicleNumber());
                    }
                    // Use the slot from the reservation record (authoritative)
                    resolvedSlotId    = reservation.getSlotId();
                    matchedReservationId = reservation.getId();
                    reservationVerified  = true;
                    log.info("Reservation verified | reservationId={} slotId={} vehicle={}",
                            matchedReservationId, resolvedSlotId, request.getVehicleNumber());
                } else {
                    // Fallback — reservation-service is down
                    log.warn("Reservation-service fallback (id=-1). "
                            + "Trusting frontend slotId | reservationId={} slotId={}",
                            request.getReservationId(), request.getSlotId());
                }
            } catch (IllegalArgumentException e) {
                throw e;   // re-throw validation errors
            } catch (Exception e) {
                log.warn("Reservation-service error during lookup. "
                        + "Trusting frontend slotId | reservationId={} error={}",
                        request.getReservationId(), e.getMessage());
            }

            // If reservation-service was unavailable, fall back to slotId from request
            if (!reservationVerified) {
                if (request.getSlotId() == null) {
                    throw new ServiceUnavailableException(
                            "Reservation service is currently unavailable and no slotId was "
                                    + "provided. Please try again.");
                }
                resolvedSlotId       = request.getSlotId();
                matchedReservationId = request.getReservationId(); // trust what frontend sent
            }

            isReservedVehicle = true;

        } else {
            // ── PATH B: WALK-IN VEHICLE ───────────────────────────────────
            log.info("Walk-in entry | vehicle={}", request.getVehicleNumber());

            if (request.getSlotId() == null) {
                log.warn("SlotId missing for walk-in | vehicle={}", request.getVehicleNumber());
                throw new IllegalArgumentException(
                        "slotId is required for walk-in entries. "
                                + "Please select an available slot.");
            }

            resolvedSlotId       = request.getSlotId();
            matchedReservationId = null;
            isReservedVehicle    = false;
        }

        // 4. Validate slot via Feign
        log.debug("Validating slot id={}", resolvedSlotId);
        SlotResponseDTO slot = parkingSlotClient.getSlotById(resolvedSlotId, authHeader);
        if ("UNKNOWN".equals(slot.getType())) {
            log.error("Slot-service unavailable | vehicle={}", request.getVehicleNumber());
            throw new ServiceUnavailableException(
                    "Slot service is currently unavailable. Please try again later.");
        }

        // 5. Slot availability check — different rules per path
        if (isReservedVehicle) {

            // ── RESERVED VEHICLE SLOT CHECK ───────────────────────────────
            // The slot is reserved FOR this vehicle. The only reason to block
            // is if another vehicle is physically parked in it right now.
            boolean physicallyOccupied = vehicleLogRepository
                    .existsBySlotIdAndStatus(resolvedSlotId, LogStatus.ACTIVE);

            if (physicallyOccupied) {
                log.warn("Reserved slot physically occupied | slotId={} reservationId={}",
                        resolvedSlotId, matchedReservationId);
                throw new SlotNotAvailableException(resolvedSlotId,
                        "Your reserved slot " + resolvedSlotId
                                + " is currently physically occupied by another vehicle. "
                                + "Please contact parking staff.");
            }

            log.debug("Reserved slot is physically free | slotId={}", resolvedSlotId);

        } else {

            // ── WALK-IN VEHICLE SLOT CHECK ────────────────────────────────
            // Walk-in vehicles may ONLY use slots that are fully available (-1).
            // Slots status 0 (reserved) or 1 (occupied) are not allowed.
            int slotStatus = slot.getOccupiedStatus();

            if (slotStatus != -1) {
                log.warn("Walk-in denied | slot not available | slotId={} status={}",
                        resolvedSlotId, slotStatus);
                String reason = slotStatus == 1 ? "occupied by another vehicle"
                                                : "reserved for another vehicle";
                throw new SlotNotAvailableException(resolvedSlotId,
                        "Slot " + resolvedSlotId + " is " + reason
                                + ". Please choose a different slot.");
            }

            // Extra physical-occupancy safety net
            boolean physicallyOccupied = vehicleLogRepository
                    .existsBySlotIdAndStatus(resolvedSlotId, LogStatus.ACTIVE);
            if (physicallyOccupied) {
                log.warn("Walk-in denied | slot physically occupied | slotId={}", resolvedSlotId);
                throw new SlotNotAvailableException(resolvedSlotId,
                        "Slot " + resolvedSlotId
                                + " is currently occupied. Please choose a different slot.");
            }

            log.debug("Walk-in slot accepted | slotId={}", resolvedSlotId);
        }

        // 6.//actual entry booking happens here
        // Mark slot as OCCUPIED (status = 1) via Feign — same for both paths
        SlotResponseDTO updated = parkingSlotClient.updateSlotStatus(
                resolvedSlotId, 1, authHeader);
        if ("UNKNOWN".equals(updated.getType())) {
            log.error("Slot-service unavailable during update | vehicle={}",
                    request.getVehicleNumber());
            throw new ServiceUnavailableException(
                    "Slot service is currently unavailable. Please try again later.");
        }
        log.info("Slot {} marked as occupied | type={}",
                resolvedSlotId, isReservedVehicle ? "RESERVED" : "WALK-IN");

        // 7. Persist vehicle log
        VehicleLog vehicleLog = VehicleLog.builder()
                .vehicleNumber(request.getVehicleNumber())
                .slotId(resolvedSlotId)
                .userId(request.getUserId())
                .reservationId(matchedReservationId)
                .entryTime(LocalDateTime.now())
                .status(LogStatus.ACTIVE)
                .build();

        VehicleLog saved = vehicleLogRepository.save(vehicleLog);
        log.info("Vehicle log created | logId={} slotId={} reservationId={} type={}",
                saved.getId(),
                saved.getSlotId(),
                saved.getReservationId(),
                saved.getReservationId() != null ? "RESERVED" : "WALK-IN");

        return toResponse(saved);
    }

    // ── EXIT ───────────────────────────────────────────────────────────────

    @Override
    public VehicleLogResponse logExit(ExitRequest request, String authHeader) {
        log.info("Exit request | vehicle={}", request.getVehicleNumber());

        // 1. Find the active log for this vehicle
        VehicleLog vehicleLog = vehicleLogRepository
                .findByVehicleNumberAndStatus(request.getVehicleNumber(), LogStatus.ACTIVE)
                .orElseThrow(() -> {
                    log.warn("No active session | vehicle={}", request.getVehicleNumber());
                    return new VehicleLogNotFoundException(
                            "No active parking session for vehicle: "
                                    + request.getVehicleNumber());
                });

        // 2. Calculate duration
        LocalDateTime exitTime = LocalDateTime.now();
        long duration = ChronoUnit.MINUTES.between(vehicleLog.getEntryTime(), exitTime);
        log.debug("Duration | minutes={}", duration);

        // 3. Free slot via Feign — set back to -1 for both entry types
        SlotResponseDTO freed = parkingSlotClient.updateSlotStatus(
                vehicleLog.getSlotId(), -1, authHeader);
        if ("UNKNOWN".equals(freed.getType())) {
            log.warn("Slot-service unavailable during exit. Slot {} may not be freed.",
                    vehicleLog.getSlotId());
        } else {
            log.info("Slot {} freed | type={}",
                    vehicleLog.getSlotId(),
                    vehicleLog.getReservationId() != null ? "RESERVED" : "WALK-IN");
        }

        // 4. Mark reservation COMPLETED — only for reserved vehicles
        if (vehicleLog.getReservationId() != null) {
            log.debug("Completing reservation id={}", vehicleLog.getReservationId());
            try {

                ReservationResponse existing =
                        reservationClient.getReservationById(vehicleLog.getReservationId());

                if (existing.getId() == -1L) {
                    log.warn("Reservation-service unavailable. Cannot fetch reservation {}.",
                            vehicleLog.getReservationId());
                } else {
                    ReservationUpdateRequest updateRequest = new ReservationUpdateRequest();
                    updateRequest.setVehicleNumber(existing.getVehicleNumber());
                    updateRequest.setStartTime(existing.getStartTime());
                    updateRequest.setEndTime(exitTime);
                    updateRequest.setStatus("COMPLETED");

                    ReservationResponse updated = reservationClient.updateReservation(
                            vehicleLog.getReservationId(), updateRequest);

                    if (updated.getId() == -1L) {
                        log.warn("Could not mark reservation {} as COMPLETED.",
                                vehicleLog.getReservationId());
                    } else {
                        log.info("Reservation {} marked as COMPLETED",
                                vehicleLog.getReservationId());
                    }
                }
            } catch (Exception e) {
                // Reservation update failure must NOT block the vehicle exit
                log.warn("Failed to update reservation {} during exit. Reason: {}",
                        vehicleLog.getReservationId(), e.getMessage());
            }
        } else {
            // Walk-in exit — reservationId was not stored during entry (e.g. reservation-service
            // was unavailable at entry time). As a fallback, search for any CONFIRMED reservation
            // matching this vehicle number and mark it COMPLETED now.
            log.info("Walk-in exit — searching for matching CONFIRMED reservation | vehicle={}",
                    request.getVehicleNumber());
            try {
                List<ReservationResponse> active = reservationClient.getActiveReservations();
                active.stream()
                        .filter(r -> request.getVehicleNumber().equals(r.getVehicleNumber()))
                        .findFirst()
                        .ifPresent(reservation -> {
                            ReservationUpdateRequest updateReq = new ReservationUpdateRequest();
                            updateReq.setVehicleNumber(reservation.getVehicleNumber());
                            updateReq.setStartTime(reservation.getStartTime());
                            updateReq.setEndTime(exitTime);
                            updateReq.setStatus("COMPLETED");
                            ReservationResponse updated =
                                    reservationClient.updateReservation(reservation.getId(), updateReq);
                            if (updated.getId() != -1L) {
                                log.info("Walk-in exit: matched and completed reservation id={} for vehicle={}",
                                        reservation.getId(), request.getVehicleNumber());
                            } else {
                                log.warn("Walk-in exit: reservation-service fallback triggered for reservationId={}",
                                        reservation.getId());
                            }
                        });
            } catch (Exception e) {
                log.warn("Walk-in exit: could not update reservation for vehicle={}. Reason: {}",
                        request.getVehicleNumber(), e.getMessage());
            }
        }

        // 5. Complete the vehicle log
        vehicleLog.setExitTime(exitTime);
        vehicleLog.setDurationMinutes(duration);
        vehicleLog.setStatus(LogStatus.COMPLETED);
        VehicleLog completed = vehicleLogRepository.save(vehicleLog);
        log.info("Log completed | logId={} duration={}min type={}",
                completed.getId(),
                duration,
                completed.getReservationId() != null ? "RESERVED" : "WALK-IN");

        return toResponse(completed);
    }

    // ── QUERIES ────────────────────────────────────────────────────────────

    @Override
    public VehicleLogResponse getLogById(Long logId) {
        log.info("Fetching log | logId={}", logId);
        VehicleLog vl = vehicleLogRepository.findById(logId)
                .orElseThrow(() -> new VehicleLogNotFoundException(
                        "Log not found with id: " + logId));
        return toResponse(vl);
    }

    @Override
    public List<VehicleLogResponse> getLogsByVehicle(String vehicleNumber) {
        log.info("Fetching logs | vehicle={}", vehicleNumber);
        return vehicleLogRepository.findByVehicleNumber(vehicleNumber)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<VehicleLogResponse> getAllActiveLogs() {
        log.info("Fetching all active logs");
        return vehicleLogRepository.findByStatus(LogStatus.ACTIVE)
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<VehicleLogResponse> getAllLogs() {
        log.info("Fetching all logs");
        return vehicleLogRepository.findAll()
                .stream().map(this::toResponse).toList();
    }

    @Override
    public List<VehicleLogResponse> getLogsByUser(Long userId) {
        log.info("Fetching logs | userId={}", userId);
        return vehicleLogRepository.findByUserId(userId)
                .stream().map(this::toResponse).toList();
    }

    // ── MAPPER ─────────────────────────────────────────────────────────────

    private VehicleLogResponse toResponse(VehicleLog vl) {
        return VehicleLogResponse.builder()
                .logId(vl.getId())
                .vehicleNumber(vl.getVehicleNumber())
                .slotId(vl.getSlotId())
                .userId(vl.getUserId())
                .reservationId(vl.getReservationId())
                .entryTime(vl.getEntryTime())
                .exitTime(vl.getExitTime())
                .durationMinutes(vl.getDurationMinutes())
                .status(vl.getStatus())
                .build();
    }
}