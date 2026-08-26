package com.invoicely.backend.controller;

import com.invoicely.backend.Service.BusinessService;
import com.invoicely.backend.dto.BusinessRequestDTO;
import com.invoicely.backend.dto.BusinessResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/businesses")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    public ResponseEntity<BusinessResponseDTO> createBusiness(@Valid @RequestBody BusinessRequestDTO requestDTO){
        BusinessResponseDTO response = businessService.createBusiness(requestDTO);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me")
    public ResponseEntity<BusinessResponseDTO> getMyProfile(Authentication authentication) {
        String email = authentication.getName(); // JWT se nikla hua email
        BusinessResponseDTO response = businessService.getBusinessByEmail(email);
        return ResponseEntity.ok(response);
    }
}
