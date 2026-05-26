package com.cts.project.reservationService.dto;

import com.cts.project.reservationService.client.ParkingResponse;
import com.cts.project.reservationService.client.UserResponse;
import com.cts.project.reservationService.entity.Reservation;
import org.springframework.stereotype.Component;

@Component
public class ReservationMapper {

    public Reservation toEntity(ReservationRequestDTO dto) {
        Reservation reservation = new Reservation();
        reservation.setVehicleNumber(dto.getVehicleNumber());
        reservation.setStartTime(dto.getStartTime());
        reservation.setEndTime(dto.getEndTime());
        return reservation;
    }

    public ReservationResponseDTO toResponseDTO(Reservation reservation,
                                                UserResponse user,
                                                ParkingResponse slot) {
        ReservationResponseDTO dto = new ReservationResponseDTO();
        dto.setId(reservation.getId());
        dto.setVehicleNumber(reservation.getVehicleNumber());
        dto.setStartTime(reservation.getStartTime());
        dto.setEndTime(reservation.getEndTime());
        dto.setStatus(reservation.getStatus());

        if (user != null) {
            dto.setUserId(user.getUserId());
            dto.setUserName(user.getName());
        }

        if (slot != null) {
            dto.setSlotId(slot.getId());
            dto.setSlotType(slot.getType());
            dto.setSlotLocation(slot.getLocation());
        }

        return dto;
    }
}