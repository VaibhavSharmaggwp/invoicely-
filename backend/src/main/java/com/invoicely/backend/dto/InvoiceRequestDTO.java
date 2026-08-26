package com.invoicely.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class InvoiceRequestDTO {
    @NotNull(message = "Customer ID is required")
    private UUID customerId; // Kis customer ka invoice hai?

    @NotNull(message = "Issue date is required")
    private LocalDate issueDate;

    @NotNull(message = "Due date is required")
    private LocalDate dueDate;

    @NotEmpty(message = "Invoice must have at least one item")
    @Valid // Yeh ensure karega ki list ke andar ke items bhi validate hon
    private List<InvoiceItemRequestDTO> items; // Saare items ki list
}
