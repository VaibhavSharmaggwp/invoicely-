package com.invoicely.backend.Service;


import com.invoicely.backend.dto.InvoiceRequestDTO;
import com.invoicely.backend.dto.InvoiceResponseDTO;
import com.invoicely.backend.dto.PublicInvoiceDTO;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.entity.Customer;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.entity.InvoiceItem;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.event.InvoiceCreatedEvent;
import com.invoicely.backend.kafka.InvoiceProducer;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.repository.CustomerRepository;
import com.invoicely.backend.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final InvoiceProducer invoiceProducer;

    // @Transactional ensure karta hai ki agar beech mein koi error aaye,
    // toh aadhi adhuri DB entry save na ho (Maan lo invoice save ho gaya par items nahi).
    // @CacheEvict: Jaise hi naya invoice create hoga, purana dashboard cache delete ho jayega
    // taaki agla dashboard load fresh data dikhaye!
    @org.springframework.cache.annotation.CacheEvict(value = "dashboard_summary", key = "#userEmail")
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

        // --- KAFKA EVENT TRIGGER ---
        // UI ko turant response bhej do, aur background me event trigger kar do
        InvoiceCreatedEvent event = InvoiceCreatedEvent.builder()
                .invoiceId(savedInvoice.getId())
                .invoiceNumber(savedInvoice.getInvoiceNumber())
                .customerEmail(customer.getEmail())
                .customerName(customer.getName())
                .totalAmount(savedInvoice.getTotalAmount())
                .build();

        invoiceProducer.sendInvoiceCreatedEvent(event);

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

    // 1. Dashboard summary calculation with Redis Caching & TTL
    // @Cacheable: Pehli baar DB se calculate karega aur Redis mein daal dega.
    // Agle 10 minute tak har call direct Redis se 1ms mein aayegi!
    @org.springframework.cache.annotation.Cacheable(value = "dashboard_summary", key = "#userEmail")
    public com.invoicely.backend.dto.DashboardSummaryDTO getDashboardSummary(String userEmail){
        System.out.println("🐢 CACHE MISS: Calculating dashboard summary from PostgreSQL database...");

        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        List<Invoice> invoices = invoiceRepository.findByBusinessId(business.getId());

        BigDecimal totalOutstanding = BigDecimal.ZERO;
        long pendingCount = 0;
        long overdueCount = 0;
        LocalDate today = LocalDate.now();

        for(Invoice inv: invoices){
            if(inv.getStatus() == InvoiceStatus.ISSUED || inv.getStatus() == InvoiceStatus.PARTIALLY_PAID){
                totalOutstanding = totalOutstanding.add(inv.getTotalAmount());
                pendingCount++;

                if(inv.getDueDate().isBefore(today)){
                    overdueCount++;
                }
            }
        }
        return com.invoicely.backend.dto.DashboardSummaryDTO.builder()
                .totalOutstanding(totalOutstanding)
                .dueThisWeek(totalOutstanding) // Simplified for demonstration
                .pendingInvoicesCount(pendingCount)
                .overdueInvoiceCount(overdueCount)
                .build();
    }

    // Public link ke liye invoice fetch karna
    public com.invoicely.backend.dto.PublicInvoiceDTO getPublicInvoice(java.util.UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new RuntimeException("Invoice not found or invalid link"));

        // Items mapping
        List<com.invoicely.backend.dto.InvoiceItemRequestDTO> publistItem = invoice.getItems().stream().map(item->{
                    com.invoicely.backend.dto.InvoiceItemRequestDTO dto = new com.invoicely.backend.dto.InvoiceItemRequestDTO();
                    dto.setDescription(item.getDescription());
                    dto.setQuantity(item.getQuantity());
                    dto.setUnitPrice(item.getUnitPrice());
                    return dto;
                }).collect(Collectors.toList());

        // Secure Public DTO return karo
        return com.invoicely.backend.dto.PublicInvoiceDTO.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .businessName(invoice.getBusiness().getName())
                .customerName(invoice.getCustomer().getName())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus().name())
                .items(publistItem)
                .build();

    }
}
