package com.invoicely.backend.Service;

import com.invoicely.backend.dto.BusinessRequestDTO;
import com.invoicely.backend.dto.BusinessResponseDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessService {
    private final BusinessRepository businessRepository;

    // new buisness create krne ka logic
    public BusinessResponseDTO createBusiness(BusinessRequestDTO requestDTO){
        // check if all emails exists in DB
        Optional<Business> existingBusiness = businessRepository.findByEmail(requestDTO.getEmail());
        if(existingBusiness.isPresent()){
            // Abhi ke liye hum simple RuntimeException fek rahe hain.
            // Aage chal ke hum custom exceptions banayenge (e.g., DuplicateEmailException)
            throw new RuntimeException("Business with the email already exists!!");
        }
        // DTO (request) ko entity (db model ) mein convert karega
        Business newBusiness = Business.builder()
                .name(requestDTO.getName())
                .email(requestDTO.getEmail())
                .phone(requestDTO.getPhone())
                .gstNumber(requestDTO.getGstNumber())
                .build();

        // Save in DB
        Business savedBusiness = businessRepository.save(newBusiness);

        // 4. Entity ko wapas DTO (Response) mein convert karke return karo
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
}
