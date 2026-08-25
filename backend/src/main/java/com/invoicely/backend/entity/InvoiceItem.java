package com.invoicely.backend.entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "InvoiceItem")
public class InvoiceItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // Foreign Key linking back to the parent Invoice
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    // Item ka naam (e.g., "Web Development Services" ya "Gaming Mouse")
    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private BigDecimal totalPrice; // quantity * unitPrice

    // Ek helper method taaki price easily calculate ho sake
    public void calculateTotalPrice(){
        if(this.quantity != null && this.unitPrice!= null){
            this.totalPrice = this.unitPrice.multiply(new BigDecimal(this.quantity));
        }
    }

}
