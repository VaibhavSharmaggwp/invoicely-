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
    public byte[] exportInvoicesToCSV(UUID businessId) {
        // 1. Database se saare invoices nikaalo
        List<Invoice> invoices = invoiceRepository.findByBusinessId(businessId);

        // 2. StringBuilder use karenge kyunki humein bohot saari strings jodni hain
        StringBuilder csvContent = new StringBuilder();

        // 3. Header Row (Excel ke column names)
        csvContent.append("Invoice Number, Customer Name, Issue Date, Due Date, Total, Amount, Status\n");

        // 4. Data Rows (Har invoice ka data comma se separate karke add karo)

        for (Invoice inv : invoices) {
            csvContent.append(inv.getInvoiceNumber()).append(",")
                    .append(inv.getCustomer().getName()).append(",")
                    .append(inv.getIssueDate()).append(",")
                    .append(inv.getDueDate()).append(",")
                    .append(inv.getTotalAmount()).append(",")
                    .append(inv.getStatus().name()).append("\n"); // \n matlab next row
        }
        // 5. String ko byte[] mein convert karo taaki user download kar sake
        return csvContent.toString().getBytes();
    }
}
