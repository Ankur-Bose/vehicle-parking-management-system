package com.cts.vpms.invoice.controller;

import com.cts.vpms.invoice.dto.AdminBillDTO;
import com.cts.vpms.invoice.dto.CustomerBillDTO;
import com.cts.vpms.invoice.dto.PaymentRequestDTO;
import com.cts.vpms.invoice.entity.Invoice;
import com.cts.vpms.invoice.enums.InvoiceStatus;
import com.cts.vpms.invoice.exceptions.InvoiceNotFoundException;
import com.cts.vpms.invoice.exceptions.UnauthorizedRoleException;
import com.cts.vpms.invoice.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Billing Management",
        description = "Invoice generation, payment processing, revenue reporting")
@Slf4j
@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    private void requireRole(String role, String... allowedRoles) {
        String normalized = role.trim().toUpperCase();
        for (String allowed : allowedRoles) {
            if (normalized.equals(allowed.toUpperCase())) {
                return;
            }
        }
        log.info("Access denied | role={}", role);
        throw UnauthorizedRoleException.forRole(role, "this endpoint");
    }

    // POST /api/billing/generate
    // Postman example:
    //   POST /api/billing/generate
    //     ?userId=1
    //     &reservationId=5       ← startTime fetched from reservation-service
    //     &logId=3               ← exitTime + vehicleNumber fetched from vehicle-log-service
    //     &slotId=2              ← slotType fetched from slot-management-service
    //   Header: X-Role: Staff
    @Operation(summary = "Generate invoice on vehicle exit",
            description = "startTime from reservation-service, exitTime+vehicleNumber from vehicle-log-service, slotType from slot-management-service.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice generated, QR returned"),
            @ApiResponse(responseCode = "403", description = "ADMIN or STAFF role required"),
            @ApiResponse(responseCode = "404", description = "Reservation, log or slot not found")
    })
    @PostMapping("/generate")
    public ResponseEntity<CustomerBillDTO> generateInvoice(

            @Parameter(description = "Must exist in users table", example = "1")
            @RequestParam(required = false) Long userId,

            @Parameter(description = "Reservation ID — startTime fetched from reservation-service", example = "5")
            @RequestParam(required = false) Long reservationId,

            @Parameter(description = "Vehicle log ID — exitTime and vehicleNumber fetched from vehicle-log-service", example = "3")
            @RequestParam Long logId,

            @Parameter(description = "Slot ID — slotType fetched automatically from slot-management-service", example = "2")
            @RequestParam(required = false) Long slotId,

            @Parameter(description = "ADMIN or STAFF required", example = "Staff")
            @RequestHeader(value = "X-Role", defaultValue = "CUSTOMER") String role
    ) {
        requireRole(role, "ADMIN", "STAFF");
        CustomerBillDTO bill = billingService.generateInvoice(
                userId, reservationId, logId, slotId
        );
        return ResponseEntity.ok(bill);
    }

    // GET /api/billing/{invoiceId}
    @Operation(summary = "Get invoice by ID",
            description = "CUSTOMER: CustomerBillDTO (amount + QR). ADMIN/STAFF: AdminBillDTO (full detail).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Invoice returned"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "403", description = "Unknown role")
    })
    @GetMapping("/{invoiceId}")
    public ResponseEntity<?> getInvoice(
            @PathVariable Long invoiceId,
            @RequestHeader(value = "X-Role", defaultValue = "CUSTOMER") String role
    ) {
        log.debug("GET invoice | invoiceId={} role={}", invoiceId, role);
        String roleUpper = role.trim().toUpperCase();

        if ("CUSTOMER".equals(roleUpper)) {
            return ResponseEntity.ok(billingService.getCustomerBill(invoiceId));
        }

        if ("ADMIN".equals(roleUpper) || "STAFF".equals(roleUpper)) {
            List<AdminBillDTO> all = billingService.getAllInvoicesForAdmin();
            AdminBillDTO found = null;
            for (AdminBillDTO dto : all) {
                if (dto.getInvoiceId().equals(invoiceId)) {
                    found = dto;
                    break;
                }
            }
            if (found == null) {
                throw InvoiceNotFoundException.forId(invoiceId);
            }
            return ResponseEntity.ok(found);
        }

        throw UnauthorizedRoleException.forRole(role, "GET /api/billing/" + invoiceId);
    }

    // GET /api/billing/all
    @Operation(summary = "Get all invoices", description = "ADMIN and STAFF only. Filter by status optionally.")
    @GetMapping("/all")
    public ResponseEntity<List<AdminBillDTO>> getAllInvoices(
            @RequestHeader(value = "X-Role", defaultValue = "CUSTOMER") String role,
            @RequestParam(required = false) InvoiceStatus status
    ) {
        requireRole(role, "ADMIN", "STAFF");
        List<AdminBillDTO> result;
        if (status != null) {
            result = billingService.getInvoicesByStatus(status);
        } else {
            result = billingService.getAllInvoicesForAdmin();
        }
        return ResponseEntity.ok(result);
    }

    // GET /api/billing/user/{userId}
    @Operation(summary = "Get invoices by user", description = "ADMIN STAFF and Customer only.")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getInvoicesByUser(
            @PathVariable Long userId,
            @RequestHeader(value = "X-Role", defaultValue = "CUSTOMER") String role
    ) {
        String roleUpper = role.trim().toUpperCase();
        if ("CUSTOMER".equals(roleUpper)) {
            return ResponseEntity.ok(billingService.getCustomerInvoicesByUser(userId));
        }
        requireRole(role, "ADMIN", "STAFF");
        return ResponseEntity.ok(billingService.getInvoicesByUser(userId));
    }

    // POST /api/billing/pay
    // Body: { "invoiceId": 1, "paymentMethod": "UPI" }
    @Operation(summary = "Process payment", description = "Marks invoice PAID. All roles allowed.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payment recorded"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Invoice not found"),
            @ApiResponse(responseCode = "409", description = "Already paid")
    })
    @PostMapping("/pay")
    public ResponseEntity<Map<String, Object>> processPayment(
            @Valid @RequestBody PaymentRequestDTO req
    ) {
        Invoice updated = billingService.processPayment(req);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("message", "Payment recorded successfully.");
        response.put("invoiceId", updated.getId());
        response.put("status", updated.getStatus());
        response.put("paymentMethod", updated.getPaymentMethod());
        return ResponseEntity.ok(response);
    }

    // GET /api/billing/revenue?from=...&to=...
    @Operation(summary = "Total revenue in date range", description = "ADMIN only.")
    @GetMapping("/revenue")
    public ResponseEntity<Map<String, Object>> getRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestHeader(value = "X-Role", defaultValue = "CUSTOMER") String role
    ) {
        requireRole(role, "ADMIN");
        BigDecimal revenue = billingService.getRevenue(from, to);
        Map<String, Object> response = new HashMap<String, Object>();
        response.put("from", from);
        response.put("to", to);
        response.put("revenue", revenue);
        return ResponseEntity.ok(response);
    }
}