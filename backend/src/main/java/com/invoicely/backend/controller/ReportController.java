package com.invoicely.backend.controller;

import com.invoicely.backend.Service.ReportService;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.repository.BusinessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final BusinessRepository businessRepository;

    @GetMapping("/export/invoices")
    public ResponseEntity<byte[]> downloadInvoiceReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        LocalDate start = (startDate != null && !startDate.trim().isEmpty())
                ? LocalDate.parse(startDate.trim())
                : null;
        LocalDate end = (endDate != null && !endDate.trim().isEmpty())
                ? LocalDate.parse(endDate.trim())
                : null;

        // Logged-in user ka businessId nikalenge
        UUID businessId = getCurrentBusinessId();

        // Service se byte array get karo
        byte[] fileBytes = reportService.exportInvoicesToCSV(businessId, start, end);

        String fileName = (start != null && end != null)
                ? "invoices_" + start + "_to_" + end + ".csv"
                : "invoices_report.csv";

        // Headers set karo taaki browser ko pata chale ki yeh ek downloadable file hai
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.setContentType(MediaType.parseMediaType("text/csv"));

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
    
    // Spring Security context se logged-in user ka email draw karke businessId nikalne ka real method
    private UUID getCurrentBusinessId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("User is not authenticated");
        }
        String userEmail = authentication.getName();
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found for email: " + userEmail));
        return business.getId();
    }
}

