package com.cts.project.reservationService.service;

import com.cts.project.reservationService.client.ParkingClient;
import com.cts.project.reservationService.client.ParkingResponse;
import com.cts.project.reservationService.client.UserClient;
import com.cts.project.reservationService.client.UserResponse;
import com.cts.project.reservationService.entity.Reservation;
import com.cts.project.reservationService.exception.ReservationNotFoundException;
import com.cts.project.reservationService.exception.SlotAlreadyBookedException;
import com.cts.project.reservationService.exception.UserException;
import com.cts.project.reservationService.repository.ReservationRepository;
import com.cts.project.reservationService.service.Impl.ReservationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

// mock the dependencies, only test the service logic
@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;
    @Mock private ParkingClient parkingClient;
    @Mock private UserClient userClient;

    @InjectMocks
    private ReservationServiceImpl reservationService;

    private Reservation reservation;
    private UserResponse userResponse;
    private ParkingResponse parkingResponse;

    @BeforeEach
    void setUp() {
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(1L);
        reservation.setSlotId(10L);
        reservation.setVehicleNumber("TN01AB1234");
        reservation.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        reservation.setStatus("CONFIRMED");

        userResponse = new UserResponse();
        userResponse.setUserId(1L);
        userResponse.setEmail("test@vpms.com");

        parkingResponse = new ParkingResponse();
        parkingResponse.setId(10L);
    }

    @Test
    void shouldCreateReservationSuccessfully() {
        // arrange — set up what mocks should return
        when(userClient.getUserById(1L)).thenReturn(userResponse);
        when(parkingClient.getSlotById(10L)).thenReturn(parkingResponse);
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any())).thenReturn(false);
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // act
        Reservation saved = reservationService.createReservation(1L, 10L, reservation);

        // assert
        assertThat(saved).isNotNull();
        assertThat(saved.getStatus()).isEqualTo("CONFIRMED");
        verify(parkingClient).updateSlotStatus(10L, 1);  // slot got marked as occupied
        verify(reservationRepository).save(any(Reservation.class));
    }

    @Test
    void shouldThrowWhenUserNotFoundDuringCreate() {
        when(userClient.getUserById(99L)).thenThrow(new RuntimeException("user not found"));

        assertThatThrownBy(() ->
                reservationService.createReservation(99L, 10L, reservation))
                .isInstanceOf(UserException.class);
    }

    @Test
    void shouldThrowWhenSlotAlreadyBooked() {
        when(userClient.getUserById(1L)).thenReturn(userResponse);
        when(parkingClient.getSlotById(10L)).thenReturn(parkingResponse);
        when(reservationRepository.existsOverlappingReservation(anyLong(), any(), any())).thenReturn(true);

        assertThatThrownBy(() ->
                reservationService.createReservation(1L, 10L, reservation))
                .isInstanceOf(SlotAlreadyBookedException.class);
    }

    @Test
    void shouldGetReservationById() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        Reservation found = reservationService.getReservationById(1L);

        assertThat(found.getId()).isEqualTo(1L);
        assertThat(found.getVehicleNumber()).isEqualTo("TN01AB1234");
    }

    @Test
    void shouldThrowWhenReservationNotFound() {
        when(reservationRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.getReservationById(999L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void shouldGetAllReservations() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        List<Reservation> all = reservationService.getAllReservations();

        assertThat(all).hasSize(1);
    }

    @Test
    void shouldGetActiveReservations() {
        when(reservationRepository.findByStatus("CONFIRMED")).thenReturn(List.of(reservation));

        List<Reservation> active = reservationService.getActiveReservations();

        assertThat(active).hasSize(1);
        assertThat(active.get(0).getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void shouldGetCancelledReservations() {
        Reservation cancelled = new Reservation();
        cancelled.setStatus("CANCELLED");
        when(reservationRepository.findByStatus("CANCELLED")).thenReturn(List.of(cancelled));

        List<Reservation> result = reservationService.getAllCancelledReservations();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void shouldGetReservationsByUserId() {
        when(userClient.getUserById(1L)).thenReturn(userResponse);
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getAllReservationsByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void shouldCancelReservation() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation cancelled = reservationService.cancelReservation(1L);

        assertThat(cancelled.getStatus()).isEqualTo("CANCELLED");
        verify(parkingClient).updateSlotStatus(10L, -1);  // slot freed
    }

    @Test
    void shouldThrowWhenCancellingAlreadyCancelledReservation() {
        reservation.setStatus("CANCELLED");
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        assertThatThrownBy(() -> reservationService.cancelReservation(1L))
                .isInstanceOf(ReservationNotFoundException.class);
    }

    @Test
    void shouldUpdateReservationVehicleNumber() {
        Reservation update = new Reservation();
        update.setVehicleNumber("KA05NEW999");

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.updateReservation(1L, update);

        assertThat(result.getVehicleNumber()).isEqualTo("KA05NEW999");
    }

    @Test
    void shouldMarkReservationCompletedWhenEndTimeSet() {
        Reservation update = new Reservation();
        update.setEndTime(LocalDateTime.of(2026, 5, 1, 12, 0));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        Reservation result = reservationService.updateReservation(1L, update);

        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        verify(parkingClient).updateSlotStatus(10L, -1);  // slot freed after completion
    }
}