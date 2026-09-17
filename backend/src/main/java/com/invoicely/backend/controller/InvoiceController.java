package com.invoicely.backend.controller;

import com.invoicely.backend.Service.InvoiceService;
import com.invoicely.backend.dto.*;
import com.invoicely.backend.entity.Invoice;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<Void> createInvoice(@RequestBody CreateInvoiceRequest requestDTO){
        // 1. Security Context se us user (business) ka email nikalo jisne API call ki hai.
        // Frontend ko apna email bhejne ki zaroorat nahi, JWT token me sab hai!
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // 2. Service ko call karke invoice create karte hain
        invoiceService.createNewInvoice(userEmail, requestDTO);

        // 3. Return 200 OK without a body (Android side par Unit / Void expect ho raha hai)
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getMyInvoices() {

        // 1. JWT token se user ka email nikalo
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // 2. Service ko call karo saari invoices lane ke liye
        List<InvoiceResponseDTO> myInvoices = invoiceService.getAllMyInvoices(userEmail);

        // 3. 200 OK ke sath list wapas bhej do
        return ResponseEntity.ok(myInvoices);
    }

    @GetMapping("dashboard-summary")
    public ResponseEntity<com.invoicely.backend.dto.DashboardSummaryDTO> getDashboardSummary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        com.invoicely.backend.dto.DashboardSummaryDTO summary = invoiceService.getDashboardSummary(userEmail);
        return ResponseEntity.ok(summary);
    }

    @GetMapping(params = {"page", "size"})
    public ResponseEntity<Page<Invoice>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();
        Page<Invoice> invoicePage = invoiceService.getInvoicesForUser(userEmail, page, size);
        return ResponseEntity.ok(invoicePage);
    }

    // Production: Get single invoice details belonging to authenticated user's business
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceDetailResponse> getInvoice(@PathVariable UUID id) {
        // 1. Extract authenticated user's email from SecurityContext
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // 2. Fetch invoice details securely ensuring tenant isolation
        InvoiceDetailResponse response = invoiceService.getInvoiceDetailsForUser(userEmail, id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<Void> recordPayment(
            @PathVariable UUID id,
            @RequestBody RecordPaymentRequest request
    ){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        invoiceService.recordPayment(userEmail, id, request);

        return ResponseEntity.ok().build();
    }

}
