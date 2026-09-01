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
public class PaymentReminderEvent {
    private UUID invoiceId;
    private String invoiceNumber;
    private String customerEmail;
    private String customerName;
    private BigDecimal totalAmount;

    // Yeh batayega ki reminder kaisa hai: "UPCOMING" (3 days left) ya "OVERDUE" (Late)
    private String reminderType;

}
