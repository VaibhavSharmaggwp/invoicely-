package com.invoicely.backend.Service;

import com.invoicely.backend.dto.BusinessProfileDTO;
import com.invoicely.backend.dto.BusinessRequestDTO;
import com.invoicely.backend.dto.BusinessResponseDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.entity.BusinessProfile;
import com.invoicely.backend.repository.BusinessProfileRepository;
import com.invoicely.backend.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;
    private final BusinessProfileRepository businessProfileRepository;

    // new buisness create krne ka logic
    public BusinessResponseDTO createBusiness(BusinessRequestDTO requestDTO){
        // check if all emails exists in DB
        Optional<Business> existingBusiness = businessRepository.findByEmail(requestDTO.getEmail());
        if(existingBusiness.isPresent()){
            throw new RuntimeException("Business with the email already exists!!");
        }
        Business newBusiness = Business.builder()
                .name(requestDTO.getName())
                .email(requestDTO.getEmail())
                .phone(requestDTO.getPhone())
                .gstNumber(requestDTO.getGstNumber())
                .build();

        Business savedBusiness = businessRepository.save(newBusiness);

        return BusinessResponseDTO.builder()
                .id(savedBusiness.getId())
                .name(savedBusiness.getName())
                .email(savedBusiness.getEmail())
                .phone(savedBusiness.getPhone())
                .gstNumber(savedBusiness.getGstNumber())
                .build();
    }

    // JWT email ke dwara Business details fetch karne ka method
    public BusinessResponseDTO getBusinessByEmail(String email) {
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business not found with email: " + email));

        return BusinessResponseDTO.builder()
                .id(business.getId())
                .name(business.getName())
                .email(business.getEmail())
                .phone(business.getPhone())
                .gstNumber(business.getGstNumber())
                .build();
    }

    // Fetch BusinessProfile for the authenticated user
    public BusinessProfileDTO getProfile(String email) {
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business not found with email: " + email));

        BusinessProfile profile = businessProfileRepository.findById(business.getId())
                .orElseGet(() -> BusinessProfile.builder()
                        .businessId(business.getId())
                        .legalEntityName(business.getName())
                        .contactEmail(business.getEmail())
                        .contactPhone(business.getPhone())
                        .gstin(business.getGstNumber())
                        .build()
                );

        return mapToDTO(profile);
    }

    // Save or update BusinessProfile (merges non-null fields to avoid erasing details)
    public BusinessProfileDTO updateProfile(String email, BusinessProfileDTO requestDTO) {
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business not found with email: " + email));

        BusinessProfile profile = businessProfileRepository.findById(business.getId())
                .orElseGet(() -> BusinessProfile.builder().businessId(business.getId()).build());

        // Merge Company Details
        if (requestDTO.getLegalEntityName() != null) profile.setLegalEntityName(requestDTO.getLegalEntityName());
        if (requestDTO.getTradeName() != null) profile.setTradeName(requestDTO.getTradeName());
        if (requestDTO.getGstin() != null) profile.setGstin(requestDTO.getGstin());
        if (requestDTO.getContactEmail() != null) profile.setContactEmail(requestDTO.getContactEmail());
        if (requestDTO.getContactPhone() != null) profile.setContactPhone(requestDTO.getContactPhone());
        if (requestDTO.getRegisteredAddress() != null) profile.setRegisteredAddress(requestDTO.getRegisteredAddress());
        if (requestDTO.getPinCode() != null) profile.setPinCode(requestDTO.getPinCode());

        // Merge Settlement / Bank Details
        if (requestDTO.getAccountHolderName() != null) profile.setAccountHolderName(requestDTO.getAccountHolderName());
        if (requestDTO.getBankName() != null) profile.setBankName(requestDTO.getBankName());
        if (requestDTO.getAccountNumber() != null) profile.setAccountNumber(requestDTO.getAccountNumber());
        if (requestDTO.getIfscCode() != null) profile.setIfscCode(requestDTO.getIfscCode());
        if (requestDTO.getUpiVpa() != null) profile.setUpiVpa(requestDTO.getUpiVpa());

        BusinessProfile savedProfile = businessProfileRepository.save(profile);
        return mapToDTO(savedProfile);
    }

    private String safeStr(String val) {
        return val != null ? val : "";
    }

    private BusinessProfileDTO mapToDTO(BusinessProfile profile) {
        return BusinessProfileDTO.builder()
                .businessId(profile.getBusinessId())
                .legalEntityName(safeStr(profile.getLegalEntityName()))
                .tradeName(safeStr(profile.getTradeName()))
                .gstin(safeStr(profile.getGstin()))
                .contactEmail(safeStr(profile.getContactEmail()))
                .contactPhone(safeStr(profile.getContactPhone()))
                .registeredAddress(safeStr(profile.getRegisteredAddress()))
                .pinCode(safeStr(profile.getPinCode()))
                .accountHolderName(safeStr(profile.getAccountHolderName()))
                .bankName(safeStr(profile.getBankName()))
                .accountNumber(safeStr(profile.getAccountNumber()))
                .ifscCode(safeStr(profile.getIfscCode()))
                .upiVpa(safeStr(profile.getUpiVpa()))
                .build();
    }
}
