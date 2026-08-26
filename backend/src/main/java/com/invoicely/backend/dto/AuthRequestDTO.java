package com.invoicely.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthRequestDTO {
    @NotBlank(message = "Google ID Token is required")
    private String googleIdToken; // Frontend yeh token bhejega
}
