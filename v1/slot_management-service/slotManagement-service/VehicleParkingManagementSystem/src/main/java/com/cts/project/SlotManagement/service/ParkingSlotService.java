package com.cts.project.SlotManagement.service;

import com.cts.project.SlotManagement.dto.AddSlotRequestDTO;
import com.cts.project.SlotManagement.dto.SlotResponseDTO;
import com.cts.project.SlotManagement.dto.UpdateSlotRequestDTO;

import java.util.List;

public interface ParkingSlotService {


    //Get all slots
    List<SlotResponseDTO> getAllSlots(String type, Integer status);

    //Get One slot by its Id
    SlotResponseDTO getSlotById(Long id);

    List<SlotResponseDTO> getAvailableSlots(String type);

    SlotResponseDTO addParkingSlot(AddSlotRequestDTO addSlotRequestDTO);

    SlotResponseDTO updateParkingSlotDetails(Long id, UpdateSlotRequestDTO updateSlotRequestDTO);

    SlotResponseDTO updateParkingSlotStatus(Long id, Integer status);

    SlotResponseDTO deleteParkingSlot(Long id);
}
