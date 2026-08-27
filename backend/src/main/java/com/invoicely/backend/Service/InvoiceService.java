package com.invoicely.backend.Service;


import com.invoicely.backend.dto.InvoiceRequestDTO;
import com.invoicely.backend.dto.InvoiceResponseDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.entity.Customer;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.entity.InvoiceItem;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.repository.CustomerRepository;
import com.invoicely.backend.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;

    // @Transactional ensure karta hai ki agar beech mein koi error aaye,
    // toh aadhi adhuri DB entry save na ho (Maan lo invoice save ho gaya par items nahi).
    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO requestDTO, String userEmail){
        // 1. Logged-in user (Business) ko DB se nikalo
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // 2. Customer ko find karo aur check karo ki kya yeh customer isi business ka hai? (Security check)
        Customer customer = customerRepository.findById(requestDTO.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        if(!customer.getBusiness().getId().equals(business.getId())){
            throw new RuntimeException("You are not authorized to bill this customer!");
        }
        // 3. Invoice entity setup karo
        Invoice invoice = Invoice.builder()
                .business(business)
                .customer(customer)
                .issueDate(requestDTO.getIssueDate())
                .dueDate(requestDTO.getDueDate())
                .status(InvoiceStatus.ISSUED) // Default status naye invoice ke liye
                // Simple auto-generation logic for invoice number (e.g., INV-98765)
                .invoiceNumber("INV-" + (System.currentTimeMillis() % 100000))
                .build();

        // 4. Items ko map karo aur Total Amount calculate karo
        BigDecimal totalInvoiceAmount = BigDecimal.ZERO;
        List<InvoiceItem> items = requestDTO.getItems().stream().map(itemDto -> {
            InvoiceItem item = InvoiceItem.builder()
                    .invoice(invoice) // Har item ko parent invoice se link kar rahe hain
                    .description(itemDto.getDescription())
                    .quantity(itemDto.getQuantity())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();

            item.calculateTotalPrice();
            return item;
    }).collect(Collectors.toList());

        // Invoice mein un items ko set karo
        invoice.setItems(items);

        // Saare items ka total price add karke Final Amount nikalo
        for (InvoiceItem item : items) {
            totalInvoiceAmount = totalInvoiceAmount.add(item.getTotalPrice());
        }
        invoice.setTotalAmount(totalInvoiceAmount);

        // 5. Database mein save karo (Cascade = All ki wajah se items khud ba khud save ho jayenge)
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 6. Clean Response DTO return karo
        return InvoiceResponseDTO.builder()
                .id(savedInvoice.getId())
                .invoiceNumber(savedInvoice.getInvoiceNumber())
                .status(savedInvoice.getStatus().name())
                .totalAmount(savedInvoice.getTotalAmount())
                .dueDate(savedInvoice.getDueDate())
                .build();
    }

    // Naya method Dashboard ke liye: Ek user ki saari invoices fetch karne ke liye
    public List<InvoiceResponseDTO> getAllMyInvoices(String userEmail){
        // 1. Pehle pata lagao kaunsa business logged in hai
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // 2. Us business ki saari invoices DB se nikal lo
        List<Invoice> invoices = invoiceRepository.findByBusinessId(business.getId());

        // 3. Database Entities (Invoice) ko wapas DTOs (InvoiceResponseDTO) mein convert karo
        // Taaki frontend ko sirf zaroori data mile (jaise amount aur status), pura DB object nahi
        return invoices.stream().map(inv -> InvoiceResponseDTO.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .status(inv.getStatus().name())
                .totalAmount(inv.getTotalAmount())
                .dueDate(inv.getDueDate())
                .build()
        ).collect(Collectors.toList());
    }
}
