package com.invoicely.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfileDTO {
    private UUID businessId;

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
