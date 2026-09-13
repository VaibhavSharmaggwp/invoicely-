package com.invoicely.backend.dto;


import lombok.Data;

@Data
public class LineItemDTO {
    private String description;
    private Integer quantity;
    private Double unitPrice;
}
