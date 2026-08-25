package com.invoicely.backend.entity;


import com.invoicely.backend.enums.InvoiceStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.catalina.LifecycleState;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Yeh invoice number hai (e.g., "INV-001"), auto-generate karenge aage chal ke
    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    // Foreign Key: Yeh invoice kis Business ki hai?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    // Foreign Key: Yeh invoice kis Customer ke naam par hai?
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    // We are using our enums here
    // STRING use karte hain taaki DB mein '0, 1, 2' ki jagah 'DRAFT', 'ISSUED' save ho.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvoiceStatus status;

    // Invoice kab issue hui aur kab payment aani chahiye
    private LocalDate issueDate;
    private LocalDate dueDate;

    // Total amount hamesha BigDecimal mein rakhte hain to avoid floating point math errors
    @Column(nullable = false)
    private BigDecimal totalAmount;

    // Ek invoice mein multiple items ho sakte hain (Jaise Laptop, Mouse, Keyboard)
    // Cascade = All ka matlab hai agar Invoice delete hui, toh uske Items bhi delete ho jayenge
    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder pattern ko empty list se start karne ke liye
    private List<InvoiceItem> items = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt; // when was last time edited

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Default state DRAFT rakhte hain naye invoice ke liye
        if(this.status == null){
            this.status = InvoiceStatus.DRAFT;
        }
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
