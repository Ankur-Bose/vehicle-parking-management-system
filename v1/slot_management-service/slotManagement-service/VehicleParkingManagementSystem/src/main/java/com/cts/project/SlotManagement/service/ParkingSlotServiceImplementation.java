package com.cts.project.SlotManagement.service;

import com.cts.project.SlotManagement.dto.AddSlotRequestDTO;
import com.cts.project.SlotManagement.dto.SlotResponseDTO;
import com.cts.project.SlotManagement.dto.UpdateSlotRequestDTO;
import com.cts.project.SlotManagement.entity.ParkingSlot;
import com.cts.project.SlotManagement.exception.InvalidInputException;
import com.cts.project.SlotManagement.exception.SlotNotFoundException;
import com.cts.project.SlotManagement.repository.ParkingSlotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Primary
public class ParkingSlotServiceImplementation implements ParkingSlotService {

    @Autowired
    ParkingSlotRepository parkingSlotRepository;

    //Get all slots
    @Override
    public List<SlotResponseDTO> getAllSlots(String type, Integer status){
        if (type != null && status != null) {
            // both provided → filter by both
            List<ParkingSlot> parkingSlotList = parkingSlotRepository.findByTypeAndOccupiedStatus(type, status);


            return parkingSlotListToSlotResponseDTOList(parkingSlotList);

        } else if (type != null) {
            // only type provided
            List<ParkingSlot> parkingSlotList =  parkingSlotRepository.findByType(type);
            return parkingSlotListToSlotResponseDTOList(parkingSlotList);

        } else if (status != null) {
            // only status provided
            List<ParkingSlot> parkingSlotList =  parkingSlotRepository.findByOccupiedStatus(status);
            return parkingSlotListToSlotResponseDTOList(parkingSlotList);


        } else {
            // nothing provided → return all
            List<ParkingSlot> parkingSlotList =  parkingSlotRepository.findAll();
            return parkingSlotListToSlotResponseDTOList(parkingSlotList);

        }
    }



    //Get One slot by its Id
    @Override
    public SlotResponseDTO getSlotById(Long id){

        ParkingSlot parkingSlot= parkingSlotRepository.findById(id).orElse(null);

        if(parkingSlot == null){
            throw new SlotNotFoundException("Slot with ID "+id+" is not found");
        }

        return parkingSlotToSlotResponseDTO(parkingSlot);
//        return null;
    }




    @Override
    public List<SlotResponseDTO> getAvailableSlots(String type){

        if(type != null){
            // status = -1 means AVAILABLE (not reserved, not occupied)
            List<ParkingSlot> parkingSlotList =  parkingSlotRepository.findByTypeAndOccupiedStatus(type, -1);
            return parkingSlotListToSlotResponseDTOList(parkingSlotList);

        }else{
            List<ParkingSlot> parkingSlotList =   parkingSlotRepository.findByOccupiedStatus(-1);
            return parkingSlotListToSlotResponseDTOList(parkingSlotList);
        }

    }


    @Override
    public SlotResponseDTO addParkingSlot(AddSlotRequestDTO addSlotRequestDTO){
        ParkingSlot parkingSlot = addSlotRequestDtoTOParkingSlot(addSlotRequestDTO);

        //Check for valid inputs
        String type = parkingSlot.getType();
        String location = parkingSlot.getLocation();
        if(type == null){
            throw  new InvalidInputException("Type is required. Cannot be empty");
        }

        if(location == null){
            throw  new InvalidInputException("Location is required. Cannot be empty");
        }

        if(!type.equals("2W") && !type.equals("4W")){

            throw new InvalidInputException("Invalid type. Use 2W or 4W");
        }

        if(!location.equals("G") && !location.equals("A") && !location.equals("B") && !location.equals("C") &&  !location.equals("D")){
            throw new InvalidInputException("Invalid type. Location should be G , A , B , C , D");
        }


        return parkingSlotToSlotResponseDTO(parkingSlotRepository.save(parkingSlot));
    }


    @Override
    public SlotResponseDTO updateParkingSlotDetails(Long id, UpdateSlotRequestDTO updateSlotRequestDTO) {

        ParkingSlot existing = parkingSlotRepository.findById(id).orElse(null);

        if(existing == null){
            throw new SlotNotFoundException("Slot not found for the id: "+id);
        }

        if(updateSlotRequestDTO.getLocation() != null){
            existing.setLocation(updateSlotRequestDTO.getLocation());
        }

        if(updateSlotRequestDTO.getType() != null){
            existing.setType(updateSlotRequestDTO.getType());
        }

        if(updateSlotRequestDTO.getOccupiedStatus() != null){
            existing.setOccupiedStatus(updateSlotRequestDTO.getOccupiedStatus());
        }



         return parkingSlotToSlotResponseDTO(parkingSlotRepository.save(existing));


    }

    @Override
    public SlotResponseDTO updateParkingSlotStatus(Long id, Integer status) {

         ParkingSlot originalParkingSlot =  parkingSlotRepository.findById(id).orElse(null);

        if(originalParkingSlot == null){
            throw new SlotNotFoundException("Slot not found for the id: "+id);
        }

         if(originalParkingSlot != null){
             originalParkingSlot.setOccupiedStatus(status);
         }

          ParkingSlot parkingSlot = parkingSlotRepository.save(originalParkingSlot);

         return parkingSlotToSlotResponseDTO(parkingSlot);


    }

    @Override
    public SlotResponseDTO deleteParkingSlot(Long id) {

        ParkingSlot parkingSlot = parkingSlotRepository.findById(id).orElse(null);

        if(parkingSlot == null){
            throw new SlotNotFoundException("Slot not found for the id: "+id);
        }

        parkingSlotRepository.delete(parkingSlot);

        return parkingSlotToSlotResponseDTO(parkingSlot);

    }

    //DTO conversion methods and helper methods

    public SlotResponseDTO parkingSlotToSlotResponseDTO(ParkingSlot parkingSlot){
        SlotResponseDTO slotResponseDTO = new SlotResponseDTO();

        slotResponseDTO.setId(parkingSlot.getId());
        slotResponseDTO.setLocation(parkingSlot.getLocation());
        slotResponseDTO.setOccupiedStatus(parkingSlot.getOccupiedStatus());
        slotResponseDTO.setType(parkingSlot.getType());

        return slotResponseDTO;

    }

    public ParkingSlot addSlotRequestDtoTOParkingSlot(AddSlotRequestDTO addSlotRequestDTO){
        ParkingSlot parkingSlot = new ParkingSlot();

        parkingSlot.setType(addSlotRequestDTO.getType());
        parkingSlot.setLocation(addSlotRequestDTO.getLocation());
        parkingSlot.setOccupiedStatus(-1); //always available when created

        return parkingSlot;
    }

    public ParkingSlot updateSlotRequestDtoTOParkingSlot(UpdateSlotRequestDTO updateSlotRequestDTO){
        ParkingSlot parkingSlot = new ParkingSlot();

        parkingSlot.setLocation(updateSlotRequestDTO.getLocation());
        parkingSlot.setType(updateSlotRequestDTO.getType());
        parkingSlot.setOccupiedStatus(updateSlotRequestDTO.getOccupiedStatus());

        return parkingSlot;
    }



    public List<SlotResponseDTO> parkingSlotListToSlotResponseDTOList(List<ParkingSlot> parkingSlotList){
        List<SlotResponseDTO> slotResponseDTOList = parkingSlotList.stream()
                .map(parkingSlot ->parkingSlotToSlotResponseDTO(parkingSlot))
                .toList();

        return slotResponseDTOList;
    }
}




