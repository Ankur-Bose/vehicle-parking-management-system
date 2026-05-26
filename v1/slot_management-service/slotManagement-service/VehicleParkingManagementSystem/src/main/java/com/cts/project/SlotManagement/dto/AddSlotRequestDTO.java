package com.cts.project.SlotManagement.dto;


public class AddSlotRequestDTO {

    private String type;
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
