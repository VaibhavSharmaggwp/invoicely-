package com.invoicely.backend.dto;


import lombok.Data;

import java.util.List;

@Data
public class CreateInvoiceRequest {
    private String customerName;
    private String customerEmail;
    private String customerAddress;
    private List<LineItemDTO> items; // Nested list for services
    private Integer taxRate;
    private String dueDate;
    private String memoNotes;
}
