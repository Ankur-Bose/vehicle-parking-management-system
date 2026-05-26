package com.cts.project.Vehicle_Logging.service.impl;

import com.cts.project.Vehicle_Logging.client.ParkingSlotClient;
import com.cts.project.Vehicle_Logging.client.UserClient;
import com.cts.project.Vehicle_Logging.dto.*;
import com.cts.project.Vehicle_Logging.entity.VehicleLog;
import com.cts.project.Vehicle_Logging.enums.LogStatus;
import com.cts.project.Vehicle_Logging.exception.ActiveSessionAlreadyExistsException;
import com.cts.project.Vehicle_Logging.exception.ServiceUnavailableException;
import com.cts.project.Vehicle_Logging.exception.SlotNotAvailableException;
import com.cts.project.Vehicle_Logging.exception.VehicleLogNotFoundException;
import com.cts.project.Vehicle_Logging.repository.VehicleLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleLogServiceImplTest {

    // ── Mocks ──────────────────────────────────────────────────────────────

    @Mock
    private VehicleLogRepository vehicleLogRepository;

    @Mock
    private ParkingSlotClient parkingSlotClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private VehicleLogServiceImpl vehicleLogService;

    // ── Shared test data ───────────────────────────────────────────────────

    private static final String VEHICLE      = "TN09AB1234";
    private static final Long   SLOT_ID      = 1L;
    private static final Long   USER_ID      = 1L;
    private static final String AUTH_HEADER  = "Bearer test-token";

    private EntryRequest  entryRequest;
    private ExitRequest   exitRequest;
    private UserResponse  validUser;
    private SlotResponseDTO availableSlot;
    private SlotResponseDTO occupiedSlot;
    private SlotResponseDTO unknownSlot;
    private VehicleLog    activeLog;
    private VehicleLog    savedLog;

    @BeforeEach
    void setUp() {
        // Entry request
        entryRequest = new EntryRequest();
        entryRequest.setVehicleNumber(VEHICLE);
        entryRequest.setSlotId(SLOT_ID);
        entryRequest.setUserId(USER_ID);

        // Exit request
        exitRequest = new ExitRequest();
        exitRequest.setVehicleNumber(VEHICLE);

        // Valid user returned by user-service
        validUser = new UserResponse();
        validUser.setId(USER_ID);
        validUser.setName("Test User");
        validUser.setEmail("test@vpms.com");
        validUser.setRole("STAFF");

        // Available slot (occupiedStatus = -1)
        availableSlot = new SlotResponseDTO();
        availableSlot.setId(SLOT_ID);
        availableSlot.setOccupiedStatus(-1);
        availableSlot.setType("2W");
        availableSlot.setLocation("A");

        // Occupied slot (occupiedStatus = 1)
        occupiedSlot = new SlotResponseDTO();
        occupiedSlot.setId(SLOT_ID);
        occupiedSlot.setOccupiedStatus(1);
        occupiedSlot.setType("2W");
        occupiedSlot.setLocation("A");

        // Fallback slot (type = UNKNOWN — returned when slot-service is down)
        unknownSlot = new SlotResponseDTO();
        unknownSlot.setId(SLOT_ID);
        unknownSlot.setOccupiedStatus(1);
        unknownSlot.setType("UNKNOWN");
        unknownSlot.setLocation("UNKNOWN");

        // Active log already in DB
        activeLog = VehicleLog.builder()
                .id(1L)
                .vehicleNumber(VEHICLE)
                .slotId(SLOT_ID)
                .userId(USER_ID)
                .entryTime(LocalDateTime.now().minusMinutes(30))
                .status(LogStatus.ACTIVE)
                .build();

        // Saved log returned after vehicleLogRepository.save()
        savedLog = VehicleLog.builder()
                .id(1L)
                .vehicleNumber(VEHICLE)
                .slotId(SLOT_ID)
                .userId(USER_ID)
                .entryTime(LocalDateTime.now())
                .status(LogStatus.ACTIVE)
                .build();
    }

    // ══════════════════════════════════════════════════════════════════════
    //  logEntry — happy path
    // ══════════════════════════════════════════════════════════════════════

    @Test
    void logEntry_Success() {
        // no active session in DB
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.empty());
        // user-service returns valid user
        when(userClient.getUserById(USER_ID, AUTH_HEADER))
                .thenReturn(validUser);
        // slot-service returns available slot
        when(parkingSlotClient.getSlotById(SLOT_ID, AUTH_HEADER))
                .thenReturn(availableSlot);
        // slot-service marks slot as occupied
        when(parkingSlotClient.updateSlotStatus(SLOT_ID, 1, AUTH_HEADER))
                .thenReturn(occupiedSlot);
        // repository saves and returns the log
        when(vehicleLogRepository.save(any(VehicleLog.class)))
                .thenReturn(savedLog);

        VehicleLogResponse response = vehicleLogService.logEntry(entryRequest, AUTH_HEADER);

        assertThat(response).isNotNull();
        assertThat(response.getVehicleNumber()).isEqualTo(VEHICLE);
        assertThat(response.getSlotId()).isEqualTo(SLOT_ID);
        assertThat(response.getStatus()).isEqualTo(LogStatus.ACTIVE);

        // verify slot was marked occupied exactly once
        verify(parkingSlotClient, times(1)).updateSlotStatus(SLOT_ID, 1, AUTH_HEADER);
        verify(vehicleLogRepository, times(1)).save(any(VehicleLog.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  logEntry — failure cases
    // ══════════════════════════════════════════════════════════════════════

    @Test
    void logEntry_ThrowsException_WhenActiveSessionExists() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.of(activeLog));

        assertThatThrownBy(() -> vehicleLogService.logEntry(entryRequest, AUTH_HEADER))
                .isInstanceOf(ActiveSessionAlreadyExistsException.class)
                .hasMessageContaining(VEHICLE);

        // user-service and slot-service must never be called
        verifyNoInteractions(userClient);
        verifyNoInteractions(parkingSlotClient);
    }

    @Test
    void logEntry_ThrowsException_WhenUserServiceDown() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.empty());

        // fallback returns sentinel id = -1
        UserResponse fallbackUser = new UserResponse();
        fallbackUser.setId(-1L);
        fallbackUser.setName("UNAVAILABLE");
        when(userClient.getUserById(USER_ID, AUTH_HEADER))
                .thenReturn(fallbackUser);

        assertThatThrownBy(() -> vehicleLogService.logEntry(entryRequest, AUTH_HEADER))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("User service");

        verifyNoInteractions(parkingSlotClient);
    }

    @Test
    void logEntry_ThrowsException_WhenSlotServiceDown_OnGetSlot() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userClient.getUserById(USER_ID, AUTH_HEADER))
                .thenReturn(validUser);
        // fallback returns UNKNOWN type
        when(parkingSlotClient.getSlotById(SLOT_ID, AUTH_HEADER))
                .thenReturn(unknownSlot);

        assertThatThrownBy(() -> vehicleLogService.logEntry(entryRequest, AUTH_HEADER))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Slot service");

        verify(parkingSlotClient, never()).updateSlotStatus(anyLong(), anyInt(), anyString());
    }

    @Test
    void logEntry_ThrowsException_WhenSlotIsOccupied() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userClient.getUserById(USER_ID, AUTH_HEADER))
                .thenReturn(validUser);
        when(parkingSlotClient.getSlotById(SLOT_ID, AUTH_HEADER))
                .thenReturn(occupiedSlot);  // occupiedStatus = 1, not -1

        assertThatThrownBy(() -> vehicleLogService.logEntry(entryRequest, AUTH_HEADER))
                .isInstanceOf(SlotNotAvailableException.class)
                .hasMessageContaining(SLOT_ID.toString());

        verify(parkingSlotClient, never()).updateSlotStatus(anyLong(), anyInt(), anyString());
    }

    @Test
    void logEntry_ThrowsException_WhenSlotServiceDown_OnUpdateSlot() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(userClient.getUserById(USER_ID, AUTH_HEADER))
                .thenReturn(validUser);
        when(parkingSlotClient.getSlotById(SLOT_ID, AUTH_HEADER))
                .thenReturn(availableSlot);
        // slot-service falls back on update
        when(parkingSlotClient.updateSlotStatus(SLOT_ID, 1, AUTH_HEADER))
                .thenReturn(unknownSlot);

        assertThatThrownBy(() -> vehicleLogService.logEntry(entryRequest, AUTH_HEADER))
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("Slot service");

        verify(vehicleLogRepository, never()).save(any());
    }

    // ══════════════════════════════════════════════════════════════════════
    //  logExit — happy path
    // ══════════════════════════════════════════════════════════════════════

    @Test
    void logExit_Success() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.of(activeLog));
        when(parkingSlotClient.updateSlotStatus(SLOT_ID, -1, AUTH_HEADER))
                .thenReturn(availableSlot);

        VehicleLog completedLog = VehicleLog.builder()
                .id(1L)
                .vehicleNumber(VEHICLE)
                .slotId(SLOT_ID)
                .userId(USER_ID)
                .entryTime(activeLog.getEntryTime())
                .exitTime(LocalDateTime.now())
                .durationMinutes(30L)
                .status(LogStatus.COMPLETED)
                .build();
        when(vehicleLogRepository.save(any(VehicleLog.class)))
                .thenReturn(completedLog);

        VehicleLogResponse response = vehicleLogService.logExit(exitRequest, AUTH_HEADER);

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo(LogStatus.COMPLETED);
        assertThat(response.getDurationMinutes()).isNotNull();

        // slot must be freed (status = -1)
        verify(parkingSlotClient, times(1)).updateSlotStatus(SLOT_ID, -1, AUTH_HEADER);
        verify(vehicleLogRepository, times(1)).save(any(VehicleLog.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  logExit — failure cases
    // ══════════════════════════════════════════════════════════════════════

    @Test
    void logExit_ThrowsException_WhenNoActiveSessionFound() {
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleLogService.logExit(exitRequest, AUTH_HEADER))
                .isInstanceOf(VehicleLogNotFoundException.class)
                .hasMessageContaining(VEHICLE);

        verifyNoInteractions(parkingSlotClient);
    }

    @Test
    void logExit_CompletesLog_EvenWhenSlotServiceDown() {
        // slot-service falls back during exit — exit should still complete
        when(vehicleLogRepository.findByVehicleNumberAndStatus(VEHICLE, LogStatus.ACTIVE))
                .thenReturn(Optional.of(activeLog));
        when(parkingSlotClient.updateSlotStatus(SLOT_ID, -1, AUTH_HEADER))
                .thenReturn(unknownSlot);   // fallback returned

        VehicleLog completedLog = VehicleLog.builder()
                .id(1L).vehicleNumber(VEHICLE).slotId(SLOT_ID).userId(USER_ID)
                .entryTime(activeLog.getEntryTime()).exitTime(LocalDateTime.now())
                .durationMinutes(30L).status(LogStatus.COMPLETED).build();
        when(vehicleLogRepository.save(any(VehicleLog.class)))
                .thenReturn(completedLog);

        // must NOT throw — exit completes even if slot-service is unavailable
        VehicleLogResponse response = vehicleLogService.logExit(exitRequest, AUTH_HEADER);

        assertThat(response.getStatus()).isEqualTo(LogStatus.COMPLETED);
        verify(vehicleLogRepository, times(1)).save(any(VehicleLog.class));
    }

    // ══════════════════════════════════════════════════════════════════════
    //  Query methods
    // ══════════════════════════════════════════════════════════════════════

    @Test
    void getLogById_ReturnsLog_WhenFound() {
        when(vehicleLogRepository.findById(1L))
                .thenReturn(Optional.of(savedLog));

        VehicleLogResponse response = vehicleLogService.getLogById(1L);

        assertThat(response.getLogId()).isEqualTo(1L);
        assertThat(response.getVehicleNumber()).isEqualTo(VEHICLE);
    }

    @Test
    void getLogById_ThrowsException_WhenNotFound() {
        when(vehicleLogRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> vehicleLogService.getLogById(99L))
                .isInstanceOf(VehicleLogNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getLogsByVehicle_ReturnsList() {
        when(vehicleLogRepository.findByVehicleNumber(VEHICLE))
                .thenReturn(List.of(savedLog));

        List<VehicleLogResponse> result = vehicleLogService.getLogsByVehicle(VEHICLE);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getVehicleNumber()).isEqualTo(VEHICLE);
    }

    @Test
    void getAllActiveLogs_ReturnsList() {
        when(vehicleLogRepository.findByStatus(LogStatus.ACTIVE))
                .thenReturn(List.of(savedLog));

        List<VehicleLogResponse> result = vehicleLogService.getAllActiveLogs();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(LogStatus.ACTIVE);
    }

    @Test
    void getAllLogs_ReturnsList() {
        when(vehicleLogRepository.findAll())
                .thenReturn(List.of(savedLog));

        List<VehicleLogResponse> result = vehicleLogService.getAllLogs();

        assertThat(result).hasSize(1);
    }
}