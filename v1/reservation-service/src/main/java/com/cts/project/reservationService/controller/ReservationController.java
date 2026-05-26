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
    import io.swagger.v3.oas.annotations.Operation;
    import io.swagger.v3.oas.annotations.Parameter;
    import io.swagger.v3.oas.annotations.responses.ApiResponse;
    import io.swagger.v3.oas.annotations.tags.Tag;
    import jakarta.validation.Valid;
    import jakarta.validation.constraints.Max;
    import jakarta.validation.constraints.Min;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.validation.annotation.Validated;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.server.ResponseStatusException;

    import java.util.List;

    @RestController
    @RequestMapping("/reservations")
    //using for bean validation
    @Validated
    //for swagger
    @Tag(name = "Reservation Controller", description = "Manages all parking reservation operations")
    public class ReservationController {

        @Autowired
        private ReservationService reservationService;

        @Autowired
        private ReservationMapper reservationMapper;

        @Autowired
        private ParkingClient parkingClient;

        @Autowired
        private UserClient userClient;

        //it takes the user data and parking slot data and combines with the reservation that returns to the reservation mapper
        private ReservationResponseDTO buildResponse(Reservation r) {
            UserResponse user = userClient.getUserById(r.getUserId());
            ParkingResponse slot = parkingClient.getSlotById(r.getSlotId());
            return reservationMapper.toResponseDTO(r, user, slot);
        }

        //small helper — checks if role from header is in the allowed list, else throws 403
        private void checkRole(String role, String... allowedRoles) {
            if (role == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "missing role header");
            }
            for (String allowed : allowedRoles) {
                if (allowed.equalsIgnoreCase(role)) return; //all good, role matches
            }
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "access denied — needs one of: " + String.join(", ", allowedRoles));
        }

        @Operation(summary = "Create a new reservation")
        @ApiResponse(responseCode = "201", description = "Reservation created successfully")
        //creating the reservation — customer, staff, admin all can create
        @PostMapping("/{userId}/{slotId}")
        public ResponseEntity<ReservationResponseDTO> createReservation(
                @PathVariable Long userId,
                @PathVariable Long slotId,
                @Valid @RequestBody ReservationRequestDTO requestDTO,
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "CUSTOMER", "STAFF", "ADMIN");

            Reservation reservation = reservationMapper.toEntity(requestDTO);
            Reservation created = reservationService.createReservation(userId, slotId, reservation);
            return ResponseEntity.status(HttpStatus.CREATED).body(buildResponse(created));
        }

        @Operation(summary = "Get reservation by ID")
        @GetMapping("/{id}")
        //getting the reservation by Id — open to all roles
        public ResponseEntity<ReservationResponseDTO> getReservationById(
                @PathVariable Long id,
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "CUSTOMER", "STAFF", "ADMIN");

            return ResponseEntity.ok(buildResponse(reservationService.getReservationById(id)));
        }

        @Operation(summary = "Get reservation by ID")
        //getting the reservation by internal -> i am using for inter service communication
        @GetMapping("internal/{id}")
        public ResponseEntity<ReservationResponseDTO> getReservationByIdInternal(@PathVariable Long id) {
            return ResponseEntity.ok(buildResponse(reservationService.getReservationById(id)));
        }

        @Operation(summary = "Get all reservations")
        //getting all the reservations — staff and admin only
        @GetMapping
        public ResponseEntity<List<ReservationResponseDTO>> getAllReservations(
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "STAFF", "ADMIN");

            return ResponseEntity.ok(
                    reservationService.getAllReservations()
                            .stream()
                            .map(this::buildResponse)
                            .toList());
        }

        @Operation(summary = "Get active reservations")
        @GetMapping("/active")
        //active reservations — staff and admin only
        public ResponseEntity<List<ReservationResponseDTO>> getActiveReservations(
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "STAFF", "ADMIN");

            return ResponseEntity.ok(
                    reservationService.getActiveReservations()
                            .stream()
                            .map(this::buildResponse)
                            .toList());
        }

        @Operation(summary = "Get cancelled reservations")
        @GetMapping("/cancelled")
        //cancelled reservations — staff and admin only
        public ResponseEntity<List<ReservationResponseDTO>> getCancelledReservations(
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "STAFF", "ADMIN");

            return ResponseEntity.ok(
                    reservationService.getAllCancelledReservations()
                            .stream()
                            .map(this::buildResponse)
                            .toList());
        }

        @Operation(summary = "Get reservations by user")
        @GetMapping("/user/{userId}")
        //reservations of a specific user — open to all roles
        public ResponseEntity<List<ReservationResponseDTO>> getAllByUser(
                @PathVariable Long userId,
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "CUSTOMER", "STAFF", "ADMIN");

            return ResponseEntity.ok(
                    reservationService.getAllReservationsByUserId(userId)
                            .stream()
                            .map(this::buildResponse)
                            .toList());
        }

        @Operation(summary = "Update a reservation")
        @PutMapping("/{id}")
        //update reservation — admin only, no one else can touch this
        public ResponseEntity<ReservationResponseDTO> updateReservation(
                @PathVariable Long id,
                @Valid @RequestBody ReservationRequestDTO requestDTO,
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "ADMIN");

            Reservation updatedData = reservationMapper.toEntity(requestDTO);
            return ResponseEntity.ok(buildResponse(reservationService.updateReservation(id, updatedData)));
        }

        @Operation(summary = "Update a reservation (internal service call — no role check)")
        @PutMapping("/internal/{id}")
        public ResponseEntity<ReservationResponseDTO> updateReservationInternal(
                @PathVariable Long id,
                @RequestBody ReservationRequestDTO requestDTO) {
            Reservation updatedData = reservationMapper.toEntity(requestDTO);
            return ResponseEntity.ok(buildResponse(reservationService.updateReservation(id, updatedData)));
        }

        @Operation(summary = "Cancel a reservation")
        @DeleteMapping("/{id}")
        //cancel reservation — all roles can cancel
        public ResponseEntity<ReservationResponseDTO> cancelReservation(
                @PathVariable Long id,
                @RequestHeader(value = "X-Auth-Role", required = false) String role) {

            checkRole(role, "CUSTOMER", "STAFF", "ADMIN");

            return ResponseEntity.ok(buildResponse(reservationService.cancelReservation(id)));
        }
    }