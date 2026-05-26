package com.cts.vpms.invoice.client;

import lombok.Getter;

@Getter
public class SlotManagementResponse {
    private Long id;
    private Integer occupiedStatus;
    private String location;
    private String type;            // "2W" or "4W"

}