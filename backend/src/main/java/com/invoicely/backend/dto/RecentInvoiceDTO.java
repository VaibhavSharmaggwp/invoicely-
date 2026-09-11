package com.invoicely.backend.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentInvoiceDTO {
    private UUID id;
    private String invoiceNumber;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
}
