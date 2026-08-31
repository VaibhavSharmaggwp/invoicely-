package com.invoicely.backend.controller;

import com.invoicely.backend.Service.InvoiceService;
import com.invoicely.backend.dto.PublicInvoiceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/api/v1/public/invoices")
@RequiredArgsConstructor
public class PublicInvoiceController {
    private final InvoiceService invoiceService;

    // 1. Returns Beautiful HTML Invoice Webpage when opened in Browser!
    @GetMapping("/{id}")
    public String viewInvoiceHtml(@PathVariable UUID id, Model model){
        PublicInvoiceDTO publicInvoiceDTO = invoiceService.getPublicInvoice(id);
        model.addAttribute("invoice", publicInvoiceDTO);
        return "public-invoice"; // renders templates/public-invoice.html
    }

    // 2. Returns JSON for API clients if needed
    @GetMapping("/{id}/json")
    @ResponseBody
    public ResponseEntity<PublicInvoiceDTO> viewInvoiceJson(@PathVariable UUID id){
        PublicInvoiceDTO publicInvoiceDTO = invoiceService.getPublicInvoice(id);
        return ResponseEntity.ok(publicInvoiceDTO);
    }
}
