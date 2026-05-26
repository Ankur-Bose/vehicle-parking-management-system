package com.cts.project.SlotManagement.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

public class UpdateSlotRequestDTO {

    @Pattern(regexp = "^(2W|4W)$" , message = "INVALID INPUT. TYPE SHOULD BE 2W OR 4W")
    private String type;

    @Pattern(regexp = "^(A|B|C|D|G)$" , message = "INVALID INPUT. Use -1, 0, or 1")
    private String location;

    @Min(value = -1, message = "INVALID INPUT. Use -1, 0, or 1")
    @Max(value = 1,message = "INVALID INPUT. Use -1, 0, or 1")
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
