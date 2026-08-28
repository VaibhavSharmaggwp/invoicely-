package com.invoicely.backend.repository;

import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    // Android Dashboard ke liye: Ek business ki saari invoices
    List<Invoice> findByBusinessId(UUID businessId);

    // Pending Invoices count krne kr liye
    List<Invoice> findByBusinessIdAndStatus(UUID businessId, InvoiceStatus status);
    List<Invoice> findByStatusAndDueDateLessThanEqual(InvoiceStatus status, java.time.LocalDate date);
}
