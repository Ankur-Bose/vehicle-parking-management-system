package com.cts.vpms.invoice.exceptions;

import com.cts.vpms.invoice.entity.Invoice;
import org.springframework.http.HttpStatus;

public class InvoiceNotFoundException extends BillingException{
    public InvoiceNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "Invoice not found");
    }

    public static InvoiceNotFoundException forId(Long invoiceId){
        return new InvoiceNotFoundException(
                "Invoice not found with ID: "+invoiceId
        );
    }
}