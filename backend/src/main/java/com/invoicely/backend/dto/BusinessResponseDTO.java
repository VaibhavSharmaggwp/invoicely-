package com.invoicely.backend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class BusinessResponseDTO {
    private UUID id;
    private String name;
    private String email;
}
