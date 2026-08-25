package com.invoicely.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BusinessRequestDTO {
    @NotBlank(message = "Business name cannot be empty")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format") // Yeh ensure karega ki email valid ho
    private String email;

    private String phone;
    private String gstNumber;
}
