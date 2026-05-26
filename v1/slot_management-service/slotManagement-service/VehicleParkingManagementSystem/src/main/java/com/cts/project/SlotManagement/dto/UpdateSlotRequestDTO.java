package com.cts.project.SlotManagement.dto;

public class UpdateSlotRequestDTO {

    private String type;
    private String location;
    private Integer occupiedStatus;


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getOccupiedStatus() {
        return occupiedStatus;
    }

    public void setOccupiedStatus(Integer occupiedStatus) {
        this.occupiedStatus = occupiedStatus;
    }
}
