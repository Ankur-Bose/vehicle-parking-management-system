package com.cts.vpms.invoice.dto;

import com.cts.vpms.invoice.enums.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request body to confirm payment for an invoice")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PaymentRequestDTO {

    @NotNull(message = "Request body to confirm payment for an invoice")
    @Schema(description = "ID of the invoice to pay", example = "1")
    private Long invoiceId;

    @NotNull(message = "Payment method must not be null")
    @Schema(description = "Payment method chosen by the customer",
            example = "UPI",
            allowableValues = {"UPI", "CASH", "CARD", "WALLET"})
    private PaymentMethod paymentMethod;
}