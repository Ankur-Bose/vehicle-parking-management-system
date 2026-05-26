package com.cts.vpms.invoice.enums;

public enum SlotType {
    TWO_WHEELER,
    FOUR_WHEELER;
    public static SlotType fromString(String type) {
        return switch (type) {
            case "2W" -> TWO_WHEELER;
            case "4W" -> FOUR_WHEELER;
            default -> throw new IllegalArgumentException("Unknown slot type: " + type);
        };
    }
}