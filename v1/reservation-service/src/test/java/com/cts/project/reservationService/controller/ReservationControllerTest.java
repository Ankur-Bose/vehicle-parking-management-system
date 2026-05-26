package com.cts.project.reservationService.controller;

import com.cts.project.reservationService.client.ParkingClient;
import com.cts.project.reservationService.client.ParkingResponse;
import com.cts.project.reservationService.client.UserClient;
import com.cts.project.reservationService.client.UserResponse;
import com.cts.project.reservationService.dto.ReservationMapper;
import com.cts.project.reservationService.dto.ReservationRequestDTO;
import com.cts.project.reservationService.dto.ReservationResponseDTO;
import com.cts.project.reservationService.entity.Reservation;
import com.cts.project.reservationService.service.ReservationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// only loads the controller layer, mocks all dependencies
@WebMvcTest(controllers = ReservationController.class)
@AutoConfigureMockMvc(addFilters = false)  // skip security filters
@TestPropertySource(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false"
})
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // mock all the things controller depends on
    @MockitoBean private ReservationService reservationService;
    @MockitoBean private ReservationMapper reservationMapper;
    @MockitoBean private ParkingClient parkingClient;
    @MockitoBean private UserClient userClient;

    private ObjectMapper objectMapper;
    private Reservation reservation;
    private ReservationResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        // jackson setup for LocalDateTime serialization
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // sample reservation used across multiple tests
        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUserId(1L);
        reservation.setSlotId(10L);
        reservation.setVehicleNumber("TN01AB1234");
        reservation.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        reservation.setStatus("CONFIRMED");

        // dto returned by mapper
        responseDTO = new ReservationResponseDTO();
        responseDTO.setId(1L);
        responseDTO.setVehicleNumber("TN01AB1234");

        // common mocks for buildResponse helper used inside controller
        when(userClient.getUserById(anyLong())).thenReturn(new UserResponse());
        when(parkingClient.getSlotById(anyLong())).thenReturn(new ParkingResponse());
        when(reservationMapper.toResponseDTO(any(), any(), any())).thenReturn(responseDTO);
    }

    // helper to build a valid reservation request dto for tests
    private ReservationRequestDTO validRequest() {
        ReservationRequestDTO req = new ReservationRequestDTO();
        req.setVehicleNumber("TN01AB1234");  // matches vehicle number pattern
        req.setStartTime(LocalDateTime.of(2026, 5, 1, 10, 0));
        return req;
    }

    @Test
    void shouldCreateReservationWhenAdminRole() throws Exception {
        when(reservationMapper.toEntity(any())).thenReturn(reservation);
        when(reservationService.createReservation(anyLong(), anyLong(), any())).thenReturn(reservation);

        mockMvc.perform(post("/reservations/1/10")
                        .header("X-Auth-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.vehicleNumber").value("TN01AB1234"));
    }

    @Test
    void shouldCreateReservationWhenStaffRole() throws Exception {
        when(reservationMapper.toEntity(any())).thenReturn(reservation);
        when(reservationService.createReservation(anyLong(), anyLong(), any())).thenReturn(reservation);

        mockMvc.perform(post("/reservations/1/10")
                        .header("X-Auth-Role", "STAFF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldCreateReservationWhenCustomerRole() throws Exception {
        when(reservationMapper.toEntity(any())).thenReturn(reservation);
        when(reservationService.createReservation(anyLong(), anyLong(), any())).thenReturn(reservation);

        mockMvc.perform(post("/reservations/1/10")
                        .header("X-Auth-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldReturn401WhenNoRoleHeader() throws Exception {
        mockMvc.perform(get("/reservations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenCustomerCallsAdminOnlyEndpoint() throws Exception {
        // customer cant get all reservations — staff and admin only
        mockMvc.perform(get("/reservations")
                        .header("X-Auth-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetAllReservationsAsAdmin() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(reservation));

        mockMvc.perform(get("/reservations")
                        .header("X-Auth-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].vehicleNumber").value("TN01AB1234"));
    }

    @Test
    void shouldGetAllReservationsAsStaff() throws Exception {
        when(reservationService.getAllReservations()).thenReturn(List.of(reservation));

        mockMvc.perform(get("/reservations")
                        .header("X-Auth-Role", "STAFF"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldGetReservationById() throws Exception {
        when(reservationService.getReservationById(1L)).thenReturn(reservation);

        mockMvc.perform(get("/reservations/1")
                        .header("X-Auth-Role", "CUSTOMER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void shouldGetActiveReservationsAsStaff() throws Exception {
        when(reservationService.getActiveReservations()).thenReturn(List.of(reservation));

        mockMvc.perform(get("/reservations/active")
                        .header("X-Auth-Role", "STAFF"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectActiveReservationsForCustomer() throws Exception {
        mockMvc.perform(get("/reservations/active")
                        .header("X-Auth-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetCancelledReservationsAsAdmin() throws Exception {
        when(reservationService.getAllCancelledReservations()).thenReturn(List.of());

        mockMvc.perform(get("/reservations/cancelled")
                        .header("X-Auth-Role", "ADMIN"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectCancelledReservationsForCustomer() throws Exception {
        mockMvc.perform(get("/reservations/cancelled")
                        .header("X-Auth-Role", "CUSTOMER"))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldGetReservationsByUser() throws Exception {
        when(reservationService.getAllReservationsByUserId(1L)).thenReturn(List.of(reservation));

        mockMvc.perform(get("/reservations/user/1")
                        .header("X-Auth-Role", "CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldUpdateReservationAsAdmin() throws Exception {
        when(reservationMapper.toEntity(any())).thenReturn(reservation);
        when(reservationService.updateReservation(anyLong(), any())).thenReturn(reservation);

        mockMvc.perform(put("/reservations/1")
                        .header("X-Auth-Role", "ADMIN")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk());
    }

    @Test
    void shouldRejectUpdateForStaffRole() throws Exception {
        // only admin can update — staff should be denied with 403
        mockMvc.perform(put("/reservations/1")
                        .header("X-Auth-Role", "STAFF")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUpdateForCustomerRole() throws Exception {
        // only admin can update — customer should be denied with 403
        mockMvc.perform(put("/reservations/1")
                        .header("X-Auth-Role", "CUSTOMER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCancelReservationAsCustomer() throws Exception {
        when(reservationService.cancelReservation(1L)).thenReturn(reservation);

        mockMvc.perform(delete("/reservations/1")
                        .header("X-Auth-Role", "CUSTOMER"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCancelReservationAsStaff() throws Exception {
        when(reservationService.cancelReservation(1L)).thenReturn(reservation);

        mockMvc.perform(delete("/reservations/1")
                        .header("X-Auth-Role", "STAFF"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldCancelReservationAsAdmin() throws Exception {
        when(reservationService.cancelReservation(1L)).thenReturn(reservation);

        mockMvc.perform(delete("/reservations/1")
                        .header("X-Auth-Role", "ADMIN"))
                .andExpect(status().isOk());
    }
}