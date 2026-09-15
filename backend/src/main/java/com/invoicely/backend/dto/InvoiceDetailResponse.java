package com.invoicely.backend.dto;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class InvoiceDetailResponse {
    private String id;
    private String invoiceNumber;
    private String status;
    private String customerName;
    private String customerEmail;
    private String issueDate;
    private String dueDate;
    private List<LineItemDTO> items;
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal grandTotal;
    private String memoNotes;
}
