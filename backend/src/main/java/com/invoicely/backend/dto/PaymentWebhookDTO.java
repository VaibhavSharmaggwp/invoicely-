package com.invoicely.backend.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class PaymentWebhookDTO {
    private UUID invoiceId;
    private String paymentId; // e.g., "pay_LKj83984j..."
    private String status; // "SUCCESS" or "FAILED"
    private BigDecimal amountPaid;
}
