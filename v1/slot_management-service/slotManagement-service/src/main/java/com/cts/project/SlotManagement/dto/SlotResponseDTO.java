package com.cts.project.SlotManagement.dto;

import lombok.Data;

@Data
public class SlotResponseDTO {

    private Long id;
    private Integer occupiedStatus; // -1 , 0 , 1
    private String location;
    private String type;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getOccupiedStatus() {
        return occupiedStatus;
    }

    public void setOccupiedStatus(Integer occupiedStatus) {
        this.occupiedStatus = occupiedStatus;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
