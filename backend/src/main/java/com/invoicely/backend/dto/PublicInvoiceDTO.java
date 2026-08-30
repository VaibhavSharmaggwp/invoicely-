package com.invoicely.backend.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class PublicInvoiceDTO {
    private String invoiceNumber;
    private String businessName;
    private String customerName;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private BigDecimal totalAmount;
    private  String status;
    private List<InvoiceItemRequestDTO> items; // Reuse kar rahe hain item list ke liye
    private String paymentUrl;
}
