package com.invoicely.backend.controller;

import com.invoicely.backend.Service.InvoiceService;
import com.invoicely.backend.dto.InvoiceRequestDTO;
import com.invoicely.backend.dto.InvoiceResponseDTO;
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

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@Valid @RequestBody InvoiceRequestDTO requestDTO){
        // 1. Security Context se us user (business) ka email nikalo jisne API call ki hai.
        // Frontend ko apna email bhejne ki zaroorat nahi, JWT token me sab hai!
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        // 2. Service ko data aur email pass karo
        InvoiceResponseDTO response = invoiceService.createInvoice(requestDTO, userEmail);

        // 3. 201 Created status ke sath wapas bhejo
        return new ResponseEntity<>(response, HttpStatus.CREATED);
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

}
