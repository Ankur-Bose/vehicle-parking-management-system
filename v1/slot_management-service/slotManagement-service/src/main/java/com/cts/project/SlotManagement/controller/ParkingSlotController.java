package com.cts.project.SlotManagement.controller;

import com.cts.project.SlotManagement.dto.AddSlotRequestDTO;
import com.cts.project.SlotManagement.dto.SlotResponseDTO;
import com.cts.project.SlotManagement.dto.UpdateSlotRequestDTO;
import com.cts.project.SlotManagement.service.ParkingSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parking Slot Controller", description = "Handles all operations related to parking slots")
@Validated
@RestController
@RequestMapping("/slots")
public class ParkingSlotController {

    private static final Logger logger = LoggerFactory.getLogger(ParkingSlotController.class);

    @Autowired
    ParkingSlotService parkingSlotService;


    @Operation(summary = "Get slot by ID", description = "Fetches a single parking slot using its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot found"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SlotResponseDTO> getParkingSlotById(
             @PathVariable @Min(value = 1, message = "The ID starts from 1") Long id
    ) {
        logger.info("Fetching Slot by ID {}", id);
        SlotResponseDTO slotResponseDTO = parkingSlotService.getSlotById(id);
        logger.info("Fetched slot successfully with ID {}", id);
        return ResponseEntity.ok(slotResponseDTO);
    }
    @GetMapping("internal/{id}")
    public ResponseEntity<SlotResponseDTO> getParkingSlotByIdInternal(
            @PathVariable @Min(value = 1, message = "The ID starts from 1") Long id
    ) {
        logger.info("Fetching Slot by ID {}", id);
        SlotResponseDTO slotResponseDTO = parkingSlotService.getSlotById(id);
        logger.info("Fetched slot successfully with ID {}", id);
        return ResponseEntity.ok(slotResponseDTO);
    }


    @Operation(summary = "Get all slots", description = "Returns all slots. Optionally filter by type (2W/4W) and status (-1, 0, 1)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slots fetched successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter value")
    })
    @GetMapping
    public ResponseEntity<List<SlotResponseDTO>> getParkingSlots(
            @Parameter(description = "Vehicle type: 2W or 4W")
            @RequestParam(required = false)
            @Pattern(regexp = "^(2W|4W)$", message = "INVALID INPUT. TYPE SHOULD BE 2W OR 4W")
            String type,

            @Parameter(description = "Slot status: -1=available, 0=reserved, 1=occupied")
            @RequestParam(required = false)
            @Min(value = -1, message = "INVALID INPUT. Use -1, 0, or 1")
            @Max(value = 1, message = "INVALID INPUT. Use -1, 0, or 1")
            Integer status
    ) {
        logger.info("Received request to fetch all slots with filters — type: {}, status: {}", type, status);
        List<SlotResponseDTO> slotResponseDTOList = parkingSlotService.getAllSlots(type, status);
        logger.info("Successfully fetched {} slots", slotResponseDTOList.size());
        return ResponseEntity.ok(slotResponseDTOList);
    }


    @Operation(summary = "Get available slots", description = "Returns all slots with status -1. Filter by type optionally")
    @ApiResponse(responseCode = "200", description = "Available slots fetched")
    @GetMapping("/available")
    public ResponseEntity<List<SlotResponseDTO>> getAvailableParkingSlots(
            @Parameter(description = "Vehicle type: 2W or 4W")
            @RequestParam(required = false)
            @Pattern(regexp = "^(2W|4W)$", message = "INVALID INPUT. TYPE SHOULD BE 2W OR 4W")
            String type
    ) {
        logger.info("Received request to fetch available slots with type filter: {}", type);
        List<SlotResponseDTO> slotResponseDTOList = parkingSlotService.getAvailableSlots(type);
        logger.info("Successfully fetched {} available slots", slotResponseDTOList.size());
        return ResponseEntity.ok(slotResponseDTOList);
    }


    @Operation(summary = "Add a new slot", description = "Creates a new parking slot. Status defaults to -1 (available)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Slot created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<SlotResponseDTO> addSlot(@RequestBody @Valid AddSlotRequestDTO addSlotRequestDTO) {
        logger.info("Received request to add new slot");
        SlotResponseDTO slotResponseDTO = parkingSlotService.addParkingSlot(addSlotRequestDTO);
        logger.info("Successfully added new slot with id: {}", slotResponseDTO.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(slotResponseDTO);
    }


    @Operation(summary = "Update slot details", description = "Updates type, location or status of an existing slot")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot updated successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SlotResponseDTO> updateSlotDetails(
            @Parameter(description = "ID of the slot to update")
            @PathVariable @Min(value = 1, message = "The ID starts from 1") Long id,
            @RequestBody @Valid UpdateSlotRequestDTO updateSlotRequestDTO
    ) {
        logger.info("Received request to update slot details for id: {}", id);
        SlotResponseDTO slotResponseDTO = parkingSlotService.updateParkingSlotDetails(id, updateSlotRequestDTO);
        logger.info("Successfully updated slot details for id: {}", id);
        return ResponseEntity.ok(slotResponseDTO);
    }


    @Operation(summary = "Update slot status", description = "Updates only the occupied status of a slot")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<SlotResponseDTO> updateSlotStatus(
            @Parameter(description = "ID of the slot")
            @PathVariable @Min(value = 1, message = "The ID starts from 1") Long id,
            @Parameter(description = "New status: -1, 0, or 1")
            @RequestParam
            @Min(value = -1, message = "INVALID INPUT. Use -1, 0, or 1")
            @Max(value = 1, message = "INVALID INPUT. Use -1, 0, or 1")
            Integer status
    ) {
        logger.info("Received request to update status of slot id: {} to: {}", id, status);
        SlotResponseDTO slotResponseDTO = parkingSlotService.updateParkingSlotStatus(id, status);
        logger.info("Successfully updated status of slot id: {} to: {}", id, status);
        return ResponseEntity.ok(slotResponseDTO);
    }


    @Operation(summary = "Delete a slot", description = "Permanently deletes a slot by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Slot deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Slot not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<SlotResponseDTO> deleteSlot(
            @Parameter(description = "ID of the slot to delete")
            @PathVariable Long id
    ) {
        logger.info("Received request to delete slot with id: {}", id);
        SlotResponseDTO slotResponseDTO = parkingSlotService.deleteParkingSlot(id);
        logger.info("Successfully deleted slot with id: {}", id);
        return ResponseEntity.ok(slotResponseDTO);
    }
}