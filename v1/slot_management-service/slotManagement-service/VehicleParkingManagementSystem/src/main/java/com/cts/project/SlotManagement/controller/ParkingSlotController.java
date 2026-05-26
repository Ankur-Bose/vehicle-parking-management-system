package com.cts.project.SlotManagement.controller;


import com.cts.project.SlotManagement.dto.AddSlotRequestDTO;
import com.cts.project.SlotManagement.dto.SlotResponseDTO;
import com.cts.project.SlotManagement.dto.UpdateSlotRequestDTO;
import com.cts.project.SlotManagement.exception.InvalidInputException;
import com.cts.project.SlotManagement.service.ParkingSlotService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/slots")
public class ParkingSlotController {

    private static final Logger logger = LoggerFactory.getLogger(ParkingSlotController.class);

    @Autowired
    ParkingSlotService parkingSlotService;

    //Get Slot by Id
    @GetMapping("/slots/{id}")
    public ResponseEntity<SlotResponseDTO> getParkingSlotById(@PathVariable Long id){

        logger.info("Fetching Slot by ID {}",id);

        SlotResponseDTO slotResponseDTO = parkingSlotService.getSlotById(id);

        logger.info("Fecthed slot successfully wit ID {}", id);

        return ResponseEntity.ok(slotResponseDTO);

    }


    //get all slots, filter by type and status.
    @GetMapping("/slots")
    public ResponseEntity<List<SlotResponseDTO>> getParkingSlots(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer status
    ){

        logger.info("Received request to fetch all slots with filters — type: {}, status: {}", type, status);

        // Step 1: Validate type if provided
        if (type != null && !type.equals("2W") && !type.equals("4W")) {
            throw new InvalidInputException("Invalid type. Use 2W or 4W");
        }

        // Step 2: Validate status if provided
        if (status != null && status != -1 && status != 0 && status != 1) {
            throw new InvalidInputException("Invalid status. Use -1, 0, or 1");
        }

        // Step 3: call service
        List<SlotResponseDTO> slotResponseDTOList = parkingSlotService.getAllSlots(type, status);
        logger.info("Successfully fetched {} slots", slotResponseDTOList.size());

        return ResponseEntity.ok(slotResponseDTOList);



    }


    //get all available slots
    @GetMapping("/slots/available")
    public ResponseEntity<List<SlotResponseDTO>> getAvailableParkingSlots(
            @RequestParam(required = false) String type
    ){

        logger.info("Received request to fetch available slots with type filter: {}", type);

        if(type != null && !type.equals("2W") && !type.equals("4W")){
            throw new InvalidInputException("Invalid type. Use 2W or 4W");
        }

        List<SlotResponseDTO> slotResponseDTOList = parkingSlotService.getAvailableSlots(type);
        logger.info("Successfully fetched {} available slots", slotResponseDTOList.size());

        return ResponseEntity.ok(slotResponseDTOList);
    }


    @PostMapping("/slots")
    public ResponseEntity<SlotResponseDTO> addSlot(@RequestBody AddSlotRequestDTO addSlotRequestDTO){
        logger.info("Received request to add new slot");
        SlotResponseDTO slotResponseDTO = parkingSlotService.addParkingSlot(addSlotRequestDTO);
        logger.info("Successfully added new slot with id: {}", slotResponseDTO.getId());

        return ResponseEntity.status(HttpStatus.CREATED).body(slotResponseDTO);
    }

    @PutMapping("/slots/{id}")
    public ResponseEntity<SlotResponseDTO> updateSlotDetails(@PathVariable Long id,@RequestBody UpdateSlotRequestDTO updateSlotRequestDTO){

        logger.info("Received request to update slot details for id: {}", id);

        SlotResponseDTO slotResponseDTO = parkingSlotService.updateParkingSlotDetails(id, updateSlotRequestDTO);

        logger.info("Successfully updated slot details for id: {}", id);


        return ResponseEntity.ok(slotResponseDTO);

    }

    @PatchMapping("/slots/{id}")
    public ResponseEntity<SlotResponseDTO> updateSlotStatus(
            @PathVariable Long id,
            @RequestParam(required = true) Integer status
    ){
        logger.info("Received request to update status of slot id: {} to: {}", id, status);


        if(status != 0 && status != 1 && status != -1){
            logger.warn("Invalid status received: {}", status);
            throw  new InvalidInputException("Invalid status. Use -1, 0, or 1");
        }

        SlotResponseDTO slotResponseDTO = parkingSlotService.updateParkingSlotStatus(id,status);
        logger.info("Successfully updated status of slot id: {} to: {}", id, status);

        return ResponseEntity.ok(slotResponseDTO);

    }

    @DeleteMapping("/slots/{id}")
    public ResponseEntity<SlotResponseDTO> deleteSlot(
            @PathVariable Long id
    ){

        logger.info("Received request to delete slot with id: {}", id);
        SlotResponseDTO slotResponseDTO =  parkingSlotService.deleteParkingSlot(id);
        logger.info("Successfully deleted slot with id: {}", id);

        return ResponseEntity.ok(slotResponseDTO);
    }




}
