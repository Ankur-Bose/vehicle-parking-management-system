package com.cts.vpms.invoice.repository;

import com.cts.vpms.invoice.entity.Invoice;
import com.cts.vpms.invoice.enums.InvoiceStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    // SELECT * FROM invoices WHERE user_id = ?
    List<Invoice> findByUserId(Long userId);

    // SELECT * FROM invoices WHERE status = ?
    List<Invoice> findByStatus(InvoiceStatus status);

    // SUM returns null when no rows match — handled in service with ZERO fallback.
    @Query("SELECT SUM(i.amount) FROM Invoice i " +
            "WHERE i.status = 'PAID' " +
            "AND i.createdAt BETWEEN :from AND :to")
    BigDecimal totalRevenueInRange(LocalDateTime from, LocalDateTime to);
}