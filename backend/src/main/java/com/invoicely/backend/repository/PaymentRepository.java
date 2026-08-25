package com.invoicely.backend.repository;

import com.invoicely.backend.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    // Ek specific invoice ke saare payments (e.g. agar partially paid hai)
    List<Payment> findByInvoiceId(UUID invoiceId);
}
