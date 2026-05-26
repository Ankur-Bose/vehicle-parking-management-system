package com.cts.project.SlotManagement.service;

import com.cts.project.SlotManagement.dto.AddSlotRequestDTO;
import com.cts.project.SlotManagement.dto.UpdateSlotRequestDTO;
import com.cts.project.SlotManagement.entity.ParkingSlot;
import com.cts.project.SlotManagement.exception.SlotNotFoundException;
import com.cts.project.SlotManagement.repository.ParkingSlotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSlotServiceImplementationTest {

    @Mock
    ParkingSlotRepository parkingSlotRepository;

    @InjectMocks
    ParkingSlotServiceImplementation service;

    ParkingSlot createSlot() {
        ParkingSlot slot = new ParkingSlot();
        slot.setId(1L);
        slot.setType("2W");
        slot.setLocation("A");
        slot.setOccupiedStatus(-1);
        return slot;
    }

    // getAllSlots tests

    @Test
    void getAllSlots() {
        when(parkingSlotRepository.findAll()).thenReturn(List.of(createSlot()));
        assertEquals(1, service.getAllSlots(null, null).size());
    }

    @Test
    void getAllSlotsByType() {
        when(parkingSlotRepository.findByType("2W")).thenReturn(List.of(createSlot()));
        assertEquals(1, service.getAllSlots("2W", null).size());
    }

    @Test
    void getAllSlotsByStatus() {
        when(parkingSlotRepository.findByOccupiedStatus(-1)).thenReturn(List.of(createSlot()));
        assertEquals(1, service.getAllSlots(null, -1).size());
    }

    @Test
    void getAllSlotsByTypeAndStatus() {
        when(parkingSlotRepository.findByTypeAndOccupiedStatus("2W", -1)).thenReturn(List.of(createSlot()));
        assertEquals(1, service.getAllSlots("2W", -1).size());
    }

    @Test
    void getAllSlotsEmpty() {
        when(parkingSlotRepository.findAll()).thenReturn(List.of());
        assertEquals(0, service.getAllSlots(null, null).size());
    }

    // getSlotById tests

    @Test
    void getSlotById() {
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(createSlot()));
        assertNotNull(service.getSlotById(1L));
    }

    @Test
    void getSlotByIdNotFound() {
        when(parkingSlotRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SlotNotFoundException.class, () -> service.getSlotById(99L));
    }

    // getAvailableSlots tests

    @Test
    void getAvailableSlots() {
        when(parkingSlotRepository.findByOccupiedStatus(-1)).thenReturn(List.of(createSlot()));
        assertEquals(1, service.getAvailableSlots(null).size());
    }

    @Test
    void getAvailableSlotsByType() {
        when(parkingSlotRepository.findByTypeAndOccupiedStatus("2W", -1)).thenReturn(List.of(createSlot()));
        assertEquals(1, service.getAvailableSlots("2W").size());
    }

    // addParkingSlot tests

    @Test
    void addParkingSlot() {
        AddSlotRequestDTO dto = new AddSlotRequestDTO();
        dto.setType("2W");
        dto.setLocation("A");
        when(parkingSlotRepository.save(any())).thenReturn(createSlot());
        assertNotNull(service.addParkingSlot(dto));
    }

    // updateParkingSlotDetails tests

    @Test
    void updateSlotDetails() {
        UpdateSlotRequestDTO dto = new UpdateSlotRequestDTO();
        dto.setLocation("B");
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(createSlot()));
        when(parkingSlotRepository.save(any())).thenReturn(createSlot());
        assertNotNull(service.updateParkingSlotDetails(1L, dto));
    }

    @Test
    void updateSlotDetailsNotFound() {
        when(parkingSlotRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SlotNotFoundException.class,
                () -> service.updateParkingSlotDetails(99L, new UpdateSlotRequestDTO()));
    }

    // updateParkingSlotStatus tests

    @Test
    void updateSlotStatus() {
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(createSlot()));
        when(parkingSlotRepository.save(any())).thenReturn(createSlot());
        assertNotNull(service.updateParkingSlotStatus(1L, 1));
    }

    @Test
    void updateSlotStatusNotFound() {
        when(parkingSlotRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SlotNotFoundException.class,
                () -> service.updateParkingSlotStatus(99L, 1));
    }

    // deleteParkingSlot tests

    @Test
    void deleteSlot() {
        when(parkingSlotRepository.findById(1L)).thenReturn(Optional.of(createSlot()));
        assertNotNull(service.deleteParkingSlot(1L));
        verify(parkingSlotRepository, times(1)).delete(any());
    }

    @Test
    void deleteSlotNotFound() {
        when(parkingSlotRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(SlotNotFoundException.class,
                () -> service.deleteParkingSlot(99L));
    }
}