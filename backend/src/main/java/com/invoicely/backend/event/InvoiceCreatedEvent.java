package com.invoicely.backend.event;

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
public class InvoiceCreatedEvent {
    // Sirf wahi data bhejo jo notification bhejne ke liye zaroori hai.
    // Pura DB object bhejne ki zaroorat nahi hoti events mein.
    private UUID invoiceId;
    private String invoiceNumber;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;
}
