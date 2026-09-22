package com.invoicely.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    // Security Lock
    // Minimum 8 chars, 1 Uppercase, 1 Lowercase, 1 Number, 1 Special Char
    @NotBlank(message = "Password cannot be empty")
    @Pattern(
            regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!_]).{8,}$",
            message = "Password must be at least 8 chars, include 1 uppercase, 1 lowercase, 1 number, and 1 special character"
    )
    private String password;
}
