package com.cts.vpms.invoice.service;

import com.cts.vpms.invoice.client.*;
import com.cts.vpms.invoice.dto.AdminBillDTO;
import com.cts.vpms.invoice.dto.CustomerBillDTO;
import com.cts.vpms.invoice.dto.PaymentRequestDTO;
import com.cts.vpms.invoice.entity.BillingRates;
import com.cts.vpms.invoice.entity.Invoice;
import com.cts.vpms.invoice.enums.InvoiceStatus;
import com.cts.vpms.invoice.enums.SlotType;
import com.cts.vpms.invoice.exceptions.InvoiceAlreadyPaidException;
import com.cts.vpms.invoice.exceptions.InvoiceNotFoundException;
import com.cts.vpms.invoice.exceptions.QRGenerationException;
import com.cts.vpms.invoice.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository invoiceRepo;
    private final QRCodeService qrService;
    private final ReservationClient reservationClient;          // ← startTime
    private final VehicleLogClient vehicleLogClient;            // ← exitTime + vehicleNumber
    private final SlotManagementClient slotManagementClient;    // ← slotType

    // GENERATE INVOICE
    @Transactional
    public CustomerBillDTO generateInvoice(
            Long userId,
            Long reservationId,
            Long logId,
            Long slotId
    ) {
        VehicleLogResponse vehicleLog = vehicleLogClient.getLogById(logId);
        LocalDateTime exitTime   = vehicleLog.getExitTime();
        String vehicleNumber     = vehicleLog.getVehicleNumber();
        log.info("Fetched exitTime={} vehicleNumber={} from logId={}", exitTime, vehicleNumber, logId);

        LocalDateTime entryTime;
        if (reservationId != null && reservationId > 0) {
            ReservationResponse reservation = reservationClient.getReservationById(reservationId);
            entryTime = reservation.getStartTime();
            log.info("Fetched startTime={} from reservationId={}", entryTime, reservationId);
        } else {
            entryTime = vehicleLog.getEntryTime();
            log.info("No reservation — using entryTime={} from logId={}", entryTime, logId);
        }

        SlotManagementResponse slot = slotManagementClient.getSlotById(slotId);
        SlotType slotType = "2W".equals(slot.getType())
                ? SlotType.TWO_WHEELER
                : SlotType.FOUR_WHEELER;
        log.info("Fetched slotType={} from slotId={}", slotType, slotId);

        log.info("Generating invoice | user={} vehicle={} slotType={} entry={} exit={}",
                userId, vehicleNumber, slotType, entryTime, exitTime);
        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        if (minutes < 1) {
            log.debug("Duration < 1 min, defaulting to 1 for vehicle={}", vehicleNumber);
            minutes = 1;
        }

        BigDecimal amount = computeAmount(minutes, slotType);
        log.debug("Billing computed | vehicle={} minutes={} amount={}", vehicleNumber, minutes, amount);

        Invoice inv = Invoice.builder()
                .userId(userId)
                .reservationId(reservationId)
                .logId(logId)
                .slotId(slotId)
                .vehicleNumber(vehicleNumber)
                .slotType(slotType.name())
                .durationMinutes(minutes)
                .amount(amount)
                .paymentMethod(null)
                .status(InvoiceStatus.PENDING)
                .build();

        Invoice saved = invoiceRepo.save(inv);
        log.info("Invoice saved | invoiceId={} userId={} amount={}", saved.getId(), userId, amount);

        String qrBase64 = "";
        try {
            qrBase64 = qrService.generatePaymentQR(saved.getId(), saved.getAmount());
        } catch (Exception e) {
            log.error("QR generation failed | invoiceId={}", saved.getId(), e);
            throw QRGenerationException.forInvoice(saved.getId(), e);
        }

        return new CustomerBillDTO(
                saved.getId(),
                vehicleNumber,
                minutes,
                saved.getAmount(),
                qrBase64,
                saved.getStatus(),
                saved.getCreatedAt()
        );
    }

    // BILLING LOGIC
    private BigDecimal computeAmount(long minutes, SlotType type) {
        boolean is2W = (type == SlotType.TWO_WHEELER);
        BigDecimal base     = is2W ? BillingRates.TWO_WHEELER_BASE      : BillingRates.FOUR_WHEELER_BASE;
        BigDecimal hourly   = is2W ? BillingRates.TWO_WHEELER_HOURLY    : BillingRates.FOUR_WHEELER_HOURLY;
        BigDecimal dailyCap = is2W ? BillingRates.TWO_WHEELER_DAILY_CAP : BillingRates.FOUR_WHEELER_DAILY_CAP;

        if (minutes <= 60) {
            return base;
        }
        long extraHours = (long) Math.ceil((minutes - 60) / 60.0);
        BigDecimal total = base
                .add(hourly.multiply(BigDecimal.valueOf(extraHours)))
                .setScale(2, RoundingMode.HALF_UP);
        return total.min(dailyCap);
    }

    // GET BILL FOR CUSTOMER
    public CustomerBillDTO getCustomerBill(Long invoiceId) {
        log.debug("Fetching customer bill | invoiceId={}", invoiceId);

        Invoice inv = invoiceRepo.findById(invoiceId)
                .orElseThrow(new Supplier<InvoiceNotFoundException>() {
                    @Override
                    public InvoiceNotFoundException get() {
                        log.warn("Invoice not found | invoiceId={}", invoiceId);
                        return InvoiceNotFoundException.forId(invoiceId);
                    }
                });

        String qrBase64 = "";
        if (inv.getStatus() == InvoiceStatus.PENDING) {
            try {
                qrBase64 = qrService.generatePaymentQR(inv.getId(), inv.getAmount());
            } catch (Exception e) {
                log.error("QR generation failed | invoiceId={}", invoiceId, e);
                throw QRGenerationException.forInvoice(invoiceId, e);
            }
        }

        return new CustomerBillDTO(
                inv.getId(),
                inv.getVehicleNumber(),
                inv.getDurationMinutes(),
                inv.getAmount(),
                qrBase64,
                inv.getStatus(),
                inv.getCreatedAt()
        );
    }

    public List<CustomerBillDTO> getCustomerInvoicesByUser(Long userId) {
        return invoiceRepo.findByUserId(userId)
                .stream()
                .map(inv -> new CustomerBillDTO(
                        inv.getId(),
                        inv.getVehicleNumber(),
                        inv.getDurationMinutes(),
                        inv.getAmount(),
                        null,
                        inv.getStatus(),
                        inv.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    // ADMIN / STAFF — get all invoices
    public List<AdminBillDTO> getAllInvoicesForAdmin() {
        List<Invoice> invoices = invoiceRepo.findAll();
        List<AdminBillDTO> result = new ArrayList<AdminBillDTO>();
        for (Invoice inv : invoices) {
            result.add(toAdminDTO(inv));
        }
        return result;
    }

    public List<AdminBillDTO> getInvoicesByStatus(InvoiceStatus status) {
        List<Invoice> invoices = invoiceRepo.findByStatus(status);
        List<AdminBillDTO> result = new ArrayList<AdminBillDTO>();
        for (Invoice inv : invoices) {
            result.add(toAdminDTO(inv));
        }
        return result;
    }

    public List<AdminBillDTO> getInvoicesByUser(Long userId) {
        List<Invoice> invoices = invoiceRepo.findByUserId(userId);
        List<AdminBillDTO> result = new ArrayList<AdminBillDTO>();
        for (Invoice inv : invoices) {
            result.add(toAdminDTO(inv));
        }
        return result;
    }

    public BigDecimal getRevenue(LocalDateTime from, LocalDateTime to) {
        log.info("Revenue query | from={} to={}", from, to);
        BigDecimal rev = invoiceRepo.totalRevenueInRange(from, to);
        return (rev == null) ? BigDecimal.ZERO : rev;
    }

    // PROCESS PAYMENT
    @Transactional
    public Invoice processPayment(PaymentRequestDTO req) {
        log.info("Processing payment | invoiceId={} method={}", req.getInvoiceId(), req.getPaymentMethod());

        Invoice inv = invoiceRepo.findById(req.getInvoiceId())
                .orElseThrow(new Supplier<InvoiceNotFoundException>() {
                    @Override
                    public InvoiceNotFoundException get() {
                        log.warn("Payment on missing invoice | invoiceId={}", req.getInvoiceId());
                        return InvoiceNotFoundException.forId(req.getInvoiceId());
                    }
                });

        if (inv.getStatus() == InvoiceStatus.PAID) {
            log.warn("Duplicate payment | invoiceId={}", req.getInvoiceId());
            throw InvoiceAlreadyPaidException.forId(req.getInvoiceId());
        }

        inv.setPaymentMethod(req.getPaymentMethod());
        inv.setStatus(InvoiceStatus.PAID);
        Invoice saved = invoiceRepo.save(inv);

        log.info("Payment recorded | invoiceId={} method={} status={}",
                saved.getId(), saved.getPaymentMethod(), saved.getStatus());
        return saved;
    }

    // PRIVATE HELPER
    private AdminBillDTO toAdminDTO(Invoice inv) {
        return new AdminBillDTO(
                inv.getId(),
                inv.getUserId(),
                inv.getVehicleNumber(),
                inv.getSlotType(),
                inv.getDurationMinutes(),
                inv.getAmount(),
                inv.getPaymentMethod(),
                inv.getStatus(),
                inv.getCreatedAt()
        );
    }
}