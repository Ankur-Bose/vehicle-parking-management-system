package com.cts.vpms.invoice.dto;


import com.cts.vpms.invoice.enums.InvoiceStatus;
import com.cts.vpms.invoice.enums.PaymentMethod;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Full invoice detail for Admin and Staff")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AdminBillDTO {

    @Schema(description = "Unique invoice ID", example = "1")
    private Long invoiceId;

    @Schema(description = "User ID who owns this invoice", example = "1")
    private Long userId;

    @Schema(description = "Vehicle number (from request, not stored in DB)", example = "TN01AB1234")
    private String vehicleNumber;

    @Schema(description = "Slot type used for billing (not stored in DB)", example = "TWO_WHEELER")
    private String slotType;

    @Schema(description = "Duration in minutes (computed, not stored)", example = "120")
    private Long durationMinutes;

    @Schema(description = "Total amount in INR", example = "20.00")
    private BigDecimal amount;

    @Schema(description = "Payment method", example = "UPI")
    private PaymentMethod paymentMethod;

    @Schema(description = "Invoice status", example = "PENDING")
    private InvoiceStatus status;

    @Schema(description = "Invoice creation timestamp")
    private LocalDateTime createdAt;
}