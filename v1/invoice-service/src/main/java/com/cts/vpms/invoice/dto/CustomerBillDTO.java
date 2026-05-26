package com.cts.vpms.invoice.dto;


import com.cts.vpms.invoice.enums.InvoiceStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Bill shown to the customer — amount and QR code for payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class CustomerBillDTO {

    @Schema(description = "Unique invoice ID", example = "1")
    private Long invoiceId;

    @Schema(description = "Vehicle number (from request, not stored in DB)", example = "TN01AB1234")
    private String vehicleNumber;

    @Schema(description = "Parking duration in minutes (computed, not stored)", example = "120")
    private Long durationMinutes;

    @Schema(description = "Total amount in INR", example = "20.00")
    private BigDecimal amount;

    @Schema(description = "Base64 PNG QR code. Prefix 'data:image/png;base64,' to render in HTML.")
    private String paymentQRCodeBase64;

    @Schema(description = "Invoice status", example = "PENDING")
    private InvoiceStatus status;

    @Schema(description = "Invoice creation timestamp")
    private LocalDateTime createdAt;
}