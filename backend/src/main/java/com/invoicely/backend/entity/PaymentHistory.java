package com.invoicely.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Har payment ek specific invoice se judi hogi
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @Column(nullable = false)
    private BigDecimal amountPaid;
    private String paymentMethod;   // e.g., "RAZORPAY", "CASH", "BANK_TRANSFER"

    private String transactionId; // Razorpay ka pay_xxxx id yahan aayega

    @CreationTimestamp
    private LocalDateTime paymentDate;
}
