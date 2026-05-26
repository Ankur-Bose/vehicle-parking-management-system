package com.cts.project.Vehicle_Logging.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserResponse {
    @JsonProperty("userId")
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String role;
}

