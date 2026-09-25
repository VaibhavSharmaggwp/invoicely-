package com.invoicely.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "business_profiles")
@Entity
public class BusinessProfile {
    @Id
    private UUID businessId; // This links directly to your User/Auth table

    // Company Details
    private String legalEntityName;
    private String tradeName;
    private String gstin;
    private String contactEmail;
    private String contactPhone;
    private String registeredAddress;
    private String pinCode;

    // Settlement / Bank Details
    private String accountHolderName;
    private String bankName;
    private String accountNumber;
    private String ifscCode;
    private String upiVpa;
}
