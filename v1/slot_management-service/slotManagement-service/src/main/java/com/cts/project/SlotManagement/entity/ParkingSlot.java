package com.cts.project.SlotManagement.entity;


import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "parking_slots")
public class ParkingSlot{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    Long id;

    @Column(name = "type")
    String type; // 2W , 4W

    @Column(name = "is_occupied")
    Integer occupiedStatus; // -1 , 0 , 1 // default = -1

    @Column(name = "location")
    String location; // floor - G, A , B , C , D


    public Long getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Integer getOccupiedStatus() {
        return occupiedStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setOccupiedStatus(Integer occupiedStatus) {
        this.occupiedStatus = occupiedStatus;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "ParkingSlot{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", isOccupied=" + occupiedStatus +
                ", location='" + location + '\'' +
                '}';
    }
}