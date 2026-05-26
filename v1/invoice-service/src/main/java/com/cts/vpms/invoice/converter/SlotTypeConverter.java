package com.cts.vpms.invoice.converter;

import com.cts.vpms.invoice.enums.SlotType;
import com.cts.vpms.invoice.exceptions.InvalidSlotTypeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SlotTypeConverter implements Converter<String, SlotType> {
    @Override
    public SlotType convert(String source) {
        String val = source.trim().toUpperCase();
        return switch (val) {
            case "TWO_WHEELER", "2W" -> SlotType.TWO_WHEELER;
            case "FOUR_WHEELER", "4W" -> SlotType.FOUR_WHEELER;
            default -> {
                log.warn("Invalid slot type received  | value = {}", source);
                try {
                    throw InvalidSlotTypeException.forValue(source);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }
}
