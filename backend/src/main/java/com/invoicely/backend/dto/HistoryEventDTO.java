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
public class HistoryEventDTO {
    private String id;
    private String type; // PAYMENT_RECEIVED, INVOICE_CREATED, OVERDUE, SYSTEM_ALERT
    private String title;
    private String subtitle;
    private String customerName;
    private String invoiceNumber;
    private UUID invoiceId;
    private BigDecimal amount;
    private String paymentMethod;
    private String transactionId;
    private String time;
    private String date;
}
