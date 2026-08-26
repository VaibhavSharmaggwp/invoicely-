package com.invoicely.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class InvoiceResponseDTO {
    private UUID id;
    private String invoiceNumber; // e.g. "INV-7382"
    private String status;        // "DRAFT", "ISSUED", etc.
    private BigDecimal totalAmount;
    private LocalDate dueDate;
}