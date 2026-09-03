package com.invoicely.backend.repository;

import com.invoicely.backend.entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, UUID> {
    // Yeh custom query saari payments ka sum nikal kar degi ek invoice ke liye
    @Query("SELECT SUM(p.amountPaid) FROM PaymentHistory p WHERE p.invoice.id = :invoiceId")
    BigDecimal getTotalPaidForInvoice(@Param("invoiceId") UUID invoiceId);
}

