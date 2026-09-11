package com.invoicely.backend.controller;

import com.invoicely.backend.Service.DashboardService;
import com.invoicely.backend.dto.DashboardSummaryResponse;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final BusinessRepository businessRepository;

    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(Authentication authentication) {
        // 1. JWT token se logged-in user ka email nikalo
        String email = authentication.getName();

        // 2. Email se Business entity fetch karo
        Business business = businessRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Business not found for user: " + email));

        // 3. Business ID ke dwara cached dashboard summary fetch karo
        DashboardSummaryResponse response = dashboardService.getDashboardSummary(business.getId());
        return ResponseEntity.ok(response);
    }
}
