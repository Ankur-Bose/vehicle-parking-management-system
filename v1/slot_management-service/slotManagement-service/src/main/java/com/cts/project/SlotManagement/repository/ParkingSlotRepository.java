package com.cts.project.SlotManagement.repository;

import com.cts.project.SlotManagement.entity.ParkingSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ParkingSlotRepository extends JpaRepository<ParkingSlot,Long> {


    public List<ParkingSlot> findByType(String type);

    public List<ParkingSlot> findByOccupiedStatus(Integer status);

    public List<ParkingSlot> findByTypeAndOccupiedStatus(String type, Integer status);

}
