package com.cts.project.SlotManagement.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AddSlotRequestDTO {

    @NotNull(message = "Type cannot be null")
    @Pattern(regexp = "^(2W|4W)$" , message = "INVALID INPUT. TYPE SHOULD BE 2W OR 4W")
    private String type;

    @NotNull(message = "Location cannot be null")
    @Pattern(regexp = "^(A|B|C|D|G)$" , message = "INVALID INPUT. Use -1, 0, or 1")
   private String location;

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

    @Override
    public String toString() {
        return "AddSlotRequestDTO{" +
                "type='" + type + '\'' +
                ", location='" + location + '\'' +
                '}';
    }
}
