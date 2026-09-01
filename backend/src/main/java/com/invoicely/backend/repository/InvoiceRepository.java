package com.invoicely.backend.repository;

import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    // Android Dashboard ke liye: Ek business ki saari invoices
    List<Invoice> findByBusinessId(UUID businessId);

    // Pending Invoices count krne kr liye
    List<Invoice> findByBusinessIdAndStatus(UUID businessId, InvoiceStatus status);
    List<Invoice> findByStatusAndDueDateLessThanEqual(InvoiceStatus status, java.time.LocalDate date);

    // Sirf wahi invoices lao jinka status 'ISSUED' (unpaid) hai aur due date exactly match karti hai
    List<Invoice> findByStatusAndDueDate(com.invoicely.backend.enums.InvoiceStatus status, java.time.LocalDate dueDate);

    // List<Invoice> ki jagah hum Page<Invoice> return karenge
    // Aur last mein Pageable object pass karenge
    Page<Invoice> findByBusinessId(UUID businessId, Pageable pageable);
}
