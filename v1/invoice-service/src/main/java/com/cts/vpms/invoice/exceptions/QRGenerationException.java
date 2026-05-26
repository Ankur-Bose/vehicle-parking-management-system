package com.cts.vpms.invoice.exceptions;

import org.springframework.http.HttpStatus;

public class QRGenerationException extends BillingException {
    public static QRGenerationException forInvoice(Long invoiceId, Throwable cause) {
        return new QRGenerationException(
                "Failed to generate QR code for invoice: " + invoiceId, cause
        );
    }
    public QRGenerationException(String message, Throwable cause) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR, "QR_GENERATION_FAILED");
        initCause(cause); // preserves full stack trace of the root cause
    }
}