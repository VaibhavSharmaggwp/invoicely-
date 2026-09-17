package com.invoicely.backend.dto;


import lombok.Data;

@Data
public class RecordPaymentRequest {
    private Double amount;
    private String paymentMethod; // e.g UPI, BANK TRANSFER.. CASH
}
