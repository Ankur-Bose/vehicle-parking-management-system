package com.cts.project.Vehicle_Logging.controller;

import com.cts.project.Vehicle_Logging.dto.EntryRequest;
import com.cts.project.Vehicle_Logging.dto.ExitRequest;
import com.cts.project.Vehicle_Logging.dto.VehicleLogResponse;
import com.cts.project.Vehicle_Logging.exception.ErrorResponse;
import com.cts.project.Vehicle_Logging.service.VehicleLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Vehicle Log", description = "APIs for logging vehicle entry and exit")
public class VehicleLogController {

    private final VehicleLogService vehicleLogService;

    @Operation(summary = "Log vehicle entry",
            description = "Creates an ACTIVE log, validates user and slot via Feign, marks slot as occupied")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Entry logged successfully",
                    content = @Content(schema = @Schema(implementation = VehicleLogResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Slot occupied or duplicate session",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Downstream service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/entry")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VehicleLogResponse> logEntry(
            @Valid @RequestBody EntryRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("POST /logs/entry | vehicle={}", request.getVehicleNumber());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(vehicleLogService.logEntry(request, authHeader));
    }




    @Operation(summary = "Log vehicle exit",
            description = "Completes the ACTIVE log, calculates duration, frees the slot via Feign")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Exit logged successfully",
                    content = @Content(schema = @Schema(implementation = VehicleLogResponse.class))),
            @ApiResponse(responseCode = "404", description = "No active session found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/exit")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VehicleLogResponse> logExit(
            @Valid @RequestBody ExitRequest request,
            @RequestHeader("Authorization") String authHeader) {
        log.info("POST /logs/exit | vehicle={}", request.getVehicleNumber());
        return ResponseEntity.ok(vehicleLogService.logExit(request, authHeader));
    }





    @Operation(summary = "Get log by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Log found"),
            @ApiResponse(responseCode = "404", description = "Log not found")
    })

    @GetMapping("/internal/{logId}")
    public ResponseEntity<VehicleLogResponse> getLogByIdInternal(@PathVariable Long logId) {
        log.info("GET /logs/internal/{}", logId);
        return ResponseEntity.ok(vehicleLogService.getLogById(logId));
    }




    @GetMapping("/{logId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<VehicleLogResponse> getLogById(@PathVariable Long logId) {
        log.info("GET /logs/{}", logId);
        return ResponseEntity.ok(vehicleLogService.getLogById(logId));
    }




    @Operation(summary = "Get all logs for a vehicle number")
    @GetMapping("/vehicle/{vehicleNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','CUSTOMER')")
    public ResponseEntity<List<VehicleLogResponse>> getLogsByVehicle(
            @PathVariable String vehicleNumber) {
        log.info("GET /logs/vehicle/{}", vehicleNumber);
        return ResponseEntity.ok(vehicleLogService.getLogsByVehicle(vehicleNumber));
    }




    @Operation(summary = "Get all currently parked vehicles")
    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<VehicleLogResponse>> getAllActiveLogs() {
        log.info("GET /logs/active");
        return ResponseEntity.ok(vehicleLogService.getAllActiveLogs());
    }

    @Operation(summary = "Get all logs for a user")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','STAFF','CUSTOMER')")
    public ResponseEntity<List<VehicleLogResponse>> getLogsByUser(@PathVariable Long userId) {
        log.info("GET /logs/user/{}", userId);
        return ResponseEntity.ok(vehicleLogService.getLogsByUser(userId));
    }



    @Operation(summary = "Get all logs — full history (active + completed)", description = "ADMIN and STAFF")
    @GetMapping({"", "/"})
    @PreAuthorize("hasAnyRole('ADMIN','STAFF')")
    public ResponseEntity<List<VehicleLogResponse>> getAllLogs() {
        log.info("GET /logs");
        return ResponseEntity.ok(vehicleLogService.getAllLogs());
    }
}
