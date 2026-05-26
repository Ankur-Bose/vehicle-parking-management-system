package com.cts.project.reservationService.service;


import com.cts.project.reservationService.entity.Reservation;

import java.util.List;

public interface ReservationService {
    public Reservation createReservation(Long userId, Long slotId, Reservation reservation);
    public Reservation getReservationById(Long id);
    public List<Reservation> getAllReservations();
    public List<Reservation> getAllCancelledReservations();
    public List<Reservation> getAllReservationsByUserId(Long userId);
    public List<Reservation> getActiveReservations();
    public Reservation cancelReservation(Long id);
    public Reservation updateReservation(Long id,Reservation updatedData);
}
