package com.invoicely.backend.controller;

import com.invoicely.backend.Service.InvoiceService;
import com.invoicely.backend.dto.PublicInvoiceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/public/invoices")
@RequiredArgsConstructor
public class PublicInvoiceController {
    private final InvoiceService invoiceService;

    // Yahan URL mein {id} aayegi, jo customer ke WhatsApp message mein hogi
    @GetMapping("/{id}")
    public ResponseEntity<PublicInvoiceDTO> viewInvoice(@PathVariable UUID id){
        PublicInvoiceDTO publicInvoiceDTO = invoiceService.getPublicInvoice(id);
        return ResponseEntity.ok(publicInvoiceDTO);
    }
}
