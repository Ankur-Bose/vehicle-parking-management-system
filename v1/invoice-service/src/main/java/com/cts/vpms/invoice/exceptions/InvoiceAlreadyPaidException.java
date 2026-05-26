package com.cts.vpms.invoice.exceptions;

import org.springframework.http.HttpStatus;

public class InvoiceAlreadyPaidException extends BillingException {
    public static InvoiceAlreadyPaidException forId(Long invoiceId) {
        return new InvoiceAlreadyPaidException("Invoice " + invoiceId + " has already been paid.");
    }
    public InvoiceAlreadyPaidException(String message) {
        // 409 Conflict — valid request, conflicts with current state
        super(message, HttpStatus.CONFLICT, "INVOICE_ALREADY_PAID");
    }
}