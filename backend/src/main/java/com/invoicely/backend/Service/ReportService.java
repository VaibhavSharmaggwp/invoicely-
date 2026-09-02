package com.invoicely.backend.Service;


import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final InvoiceRepository invoiceRepository;

    // Yeh method bas ek plain text file ko byte array banakar return karega
    public byte[] exportInvoicesToCSV(UUID businessId, java.time.LocalDate startDate, java.time.LocalDate endDate) {

        // 1. Database se selected dates ke invoices nikaalo (or all if dates not provided)
        List<Invoice> invoices;
        if (startDate != null && endDate != null) {
            invoices = invoiceRepository.findByBusinessIdAndIssueDateBetween(businessId, startDate, endDate);
        } else {
            invoices = invoiceRepository.findByBusinessId(businessId);
        }

        StringBuilder csvContent = new StringBuilder();

        // 2. Header Row
        csvContent.append("Invoice Number,Customer Name,Issue Date,Due Date,Total Amount,Status\n");

        // 3. Data Rows with "Comma Bug" fix & null safety
        for (Invoice inv : invoices) {
            String invoiceNumber = inv.getInvoiceNumber() != null ? inv.getInvoiceNumber() : "";
            String customerName = (inv.getCustomer() != null && inv.getCustomer().getName() != null)
                    ? inv.getCustomer().getName()
                    : "N/A";
            String status = inv.getStatus() != null ? inv.getStatus().name() : "";

            // Text fields ke aaspas escaped quotes (\") lagaye hain.
            // Agar customer ka naam "Sharma, Inc." hai, toh CSV break nahi hoga.
            csvContent.append("\"").append(invoiceNumber).append("\",")
                    .append("\"").append(customerName).append("\",")
                    .append(inv.getIssueDate()).append(",")
                    .append(inv.getDueDate()).append(",")
                    .append(inv.getTotalAmount()).append(",")
                    .append("\"").append(status).append("\"\n");
        }

        return csvContent.toString().getBytes();
    }
}
