package com.cts.vpms.invoice.entity;

import com.cts.vpms.invoice.enums.InvoiceStatus;
import com.cts.vpms.invoice.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "vehicle_number", nullable = false, length = 20)
    private String vehicleNumber;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "log_id", nullable = false)
    private Long logId;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = true)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column
    private String slotType;

    @Column
    private Long durationMinutes;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = InvoiceStatus.PENDING;
        }
    }
}