package com.invoicely.backend.Service;


import com.invoicely.backend.Service.RazorpayService;
import com.invoicely.backend.dto.*;

import java.time.format.DateTimeFormatter;
import java.util.Locale;
import com.invoicely.backend.entity.Business;
import com.invoicely.backend.entity.Customer;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.entity.InvoiceItem;
import com.invoicely.backend.entity.PaymentHistory;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.event.InvoiceCreatedEvent;
import com.invoicely.backend.kafka.InvoiceProducer;
import com.invoicely.backend.repository.BusinessRepository;
import com.invoicely.backend.repository.CustomerRepository;
import com.invoicely.backend.repository.InvoiceRepository;
import com.invoicely.backend.repository.PaymentHistoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final CustomerRepository customerRepository;
    private final BusinessRepository businessRepository;
    private final InvoiceProducer invoiceProducer;
    private final RazorpayService razorpayService;
    private final PaymentHistoryRepository paymentRepository;
    private final CacheManager cacheManager;
    


    // Overloaded method using userEmail from JWT to find business and evict dashboard cache
    @org.springframework.cache.annotation.CacheEvict(value = "dashboard_summary", key = "#userEmail")
    @Transactional
    public InvoiceResponseDTO createNewInvoice(String userEmail, CreateInvoiceRequest request) {
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return createNewInvoice(business.getId(), request);
    }

    // @Transactional ensures ki agar item save hote waqt error aaye, toh poora invoice cancel ho jaye (Rollback)
    @Transactional
    public InvoiceResponseDTO createNewInvoice(UUID businessId, CreateInvoiceRequest request) {
        // 1. Fetch the business
        Business business = businessRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        // 2. Fetch or create Customer entity based on request.getCustomerName()
        Customer customer = customerRepository.findByBusinessId(businessId).stream()
                .filter(c -> c.getName() != null && c.getName().equalsIgnoreCase(request.getCustomerName().trim()))
                .findFirst()
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .name(request.getCustomerName().trim())
                            .email(request.getCustomerEmail() != null && !request.getCustomerEmail().isBlank() 
                                    ? request.getCustomerEmail().trim() : null)
                            .phone(request.getCustomerAddress() != null && !request.getCustomerAddress().isBlank() 
                                    ? request.getCustomerAddress().trim() : null)
                            .business(business)
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // 3. Calculate math securely on the server
        BigDecimal subtotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (LineItemDTO item : request.getItems()) {
                BigDecimal qty = new BigDecimal(item.getQuantity() != null ? item.getQuantity() : 1);
                BigDecimal price = BigDecimal.valueOf(item.getUnitPrice() != null ? item.getUnitPrice() : 0.0);
                subtotal = subtotal.add(qty.multiply(price));
            }
        }

        // Calculate Tax
        BigDecimal grandTotal = subtotal;
        if (request.getTaxRate() != null && request.getTaxRate() > 0) {
            BigDecimal taxMultiplier = BigDecimal.valueOf(request.getTaxRate())
                    .divide(BigDecimal.valueOf(100), 4, java.math.RoundingMode.HALF_UP);
            BigDecimal taxAmount = subtotal.multiply(taxMultiplier);
            grandTotal = subtotal.add(taxAmount);
        }

        // 4. Parse the date string coming from Android ("14 Oct, 2026")
        LocalDate dueDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.US);
            dueDate = LocalDate.parse(request.getDueDate(), formatter);
        } catch (Exception e) {
            try {
                dueDate = LocalDate.parse(request.getDueDate());
            } catch (Exception ex) {
                dueDate = LocalDate.now().plusDays(15);
            }
        }

        // 5. Build the Invoice Entity
        Invoice invoice = Invoice.builder()
                .business(business)
                .customer(customer)
                .invoiceNumber("INV-" + System.currentTimeMillis())
                .totalAmount(grandTotal)
                .status(InvoiceStatus.ISSUED)
                .memo(request.getMemoNotes())
                .dueDate(dueDate)
                .issueDate(LocalDate.now())
                .build();

        // 6. Map and save individual Line Items into the InvoiceItem table
        if (request.getItems() != null) {
            List<InvoiceItem> items = request.getItems().stream().map(itemDto -> {
                int qty = itemDto.getQuantity() != null ? itemDto.getQuantity() : 1;
                BigDecimal unitPrice = BigDecimal.valueOf(itemDto.getUnitPrice() != null ? itemDto.getUnitPrice() : 0.0);
                InvoiceItem item = InvoiceItem.builder()
                        .invoice(invoice)
                        .description(itemDto.getDescription() != null && !itemDto.getDescription().isBlank() 
                                ? itemDto.getDescription() : "Service")
                        .quantity(qty)
                        .unitPrice(unitPrice)
                        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(qty)))
                        .build();
                return item;
            }).collect(Collectors.toList());
            invoice.setItems(items);
        }

        // 7. Save to Database (CascadeType.ALL saves items automatically)
        Invoice savedInvoice = invoiceRepository.save(invoice);

        // 8. Kafka Event Trigger
        try {
            InvoiceCreatedEvent event = InvoiceCreatedEvent.builder()
                    .invoiceId(savedInvoice.getId())
                    .invoiceNumber(savedInvoice.getInvoiceNumber())
                    .customerEmail(customer.getEmail())
                    .customerName(customer.getName())
                    .totalAmount(savedInvoice.getTotalAmount())
                    .build();
            invoiceProducer.sendInvoiceCreatedEvent(event);
        } catch (Exception e) {
            System.err.println("Warning: Kafka event skipped: " + e.getMessage());
        }

        // 9. Return clean response DTO
        return InvoiceResponseDTO.builder()
                .id(savedInvoice.getId())
                .invoiceNumber(savedInvoice.getInvoiceNumber())
                .status(savedInvoice.getStatus().name())
                .totalAmount(savedInvoice.getTotalAmount())
                .dueDate(savedInvoice.getDueDate())
                .build();
    }

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
    @Transactional
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

        // Agar invoice ISSUED hai, toh naya payment link generate karo
        String paymentLink = null;
        if (invoice.getStatus() == InvoiceStatus.ISSUED) {
            paymentLink = razorpayService.createPaymentLink(invoice);
        }

        // Secure Public DTO return karo
        return com.invoicely.backend.dto.PublicInvoiceDTO.builder()
                .invoiceNumber(invoice.getInvoiceNumber())
                .businessName(invoice.getBusiness().getName())
                .customerName(invoice.getCustomer().getName())
                .issueDate(invoice.getIssueDate())
                .dueDate(invoice.getDueDate())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus().name())
                .paymentUrl(paymentLink)
                .items(publistItem)
                .build();

    }

    public Page<Invoice> getInvoicesForBusiness(UUID businessId, int pageNumber, int pageSize){
        // 1. PageRequest banate hain (Page 0 se start hota hai)
        // Hum unko latest pehle dikhana chahte hain, isliye Sort by createdAt DESC
        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by("createdAt").descending());
        // 2. Database se sirf us specific page ka data nikalte hain
        return invoiceRepository.findByBusinessId(businessId, pageable);

    }

    public Page<Invoice> getInvoicesForUser(String userEmail, int pageNumber, int pageSize){
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return getInvoicesForBusiness(business.getId(), pageNumber, pageSize);
    }

    // Overloaded method extracting business from userEmail in SecurityContext
    @Transactional
    public InvoiceDetailResponse getInvoiceDetailsForUser(String userEmail, UUID invoiceId) {
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return getInvoiceDetails(invoiceId, business.getId());
    }

    // Fetch a single invoice with its line items (ensuring tenant isolation via businessId)
    @Transactional
    public InvoiceDetailResponse getInvoiceDetails(UUID invoiceId, UUID businessId){
        // 1. Fetch the invoice from PostgreSQL with businessId security check
        Invoice invoice = invoiceRepository.findByIdAndBusinessId(invoiceId, businessId)
                .orElseThrow(() -> new RuntimeException("Invoice not found or unauthorized"));

        // 2. Map Database Line Items to DTOs
        List<LineItemDTO> itemDTOS = invoice.getItems().stream().map(item -> {
            LineItemDTO dto = new LineItemDTO();
            dto.setDescription(item.getDescription());
            dto.setQuantity(item.getQuantity());
            dto.setUnitPrice(item.getUnitPrice() != null ? item.getUnitPrice().doubleValue() : 0.0);
            return dto;
        }).toList();

        // 3. Calculate Subtotal & Tax
        BigDecimal subtotal = BigDecimal.ZERO;
        if (invoice.getItems() != null && !invoice.getItems().isEmpty()) {
            for (InvoiceItem item : invoice.getItems()) {
                if (item.getTotalPrice() != null) {
                    subtotal = subtotal.add(item.getTotalPrice());
                } else if (item.getUnitPrice() != null && item.getQuantity() != null) {
                    subtotal = subtotal.add(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
                }
            }
        } else {
            subtotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        }

        BigDecimal grandTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : subtotal;
        BigDecimal taxAmount = grandTotal.compareTo(subtotal) > 0 
                ? grandTotal.subtract(subtotal) 
                : BigDecimal.ZERO;

        // 4. Format Dates
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale.US);

        String customerName = invoice.getCustomer() != null ? invoice.getCustomer().getName() : "N/A";
        String customerEmail = invoice.getCustomer() != null && invoice.getCustomer().getEmail() != null 
                ? invoice.getCustomer().getEmail() : "";

        return InvoiceDetailResponse.builder()
                .id(invoice.getId().toString())
                .invoiceNumber(invoice.getInvoiceNumber())
                .status(invoice.getStatus().name())
                .customerName(customerName)
                .customerEmail(customerEmail)
                .issueDate(invoice.getIssueDate() != null ? invoice.getIssueDate().format(formatter) : "")
                .dueDate(invoice.getDueDate() != null ? invoice.getDueDate().format(formatter) : "")
                .items(itemDTOS)
                .subtotal(subtotal)
                .taxAmount(taxAmount)
                .grandTotal(grandTotal)
                .memoNotes(invoice.getMemo() != null ? invoice.getMemo() : "")
                .build();
    }

    // 🚀 CACHE EVICT: Payment receive hote hi Dashboard ka cache clear karna zaroori hai!
    @Transactional
    @CacheEvict(value = "dashboard_summary", key = "#businessId")
    public void recordPayment(UUID invoiceId, UUID businessId, RecordPaymentRequest request) {
        
        // 0. Validation: Amount must be provided and greater than zero
        if (request == null || request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }

        // 1. Fetch the Invoice safely
        Invoice invoice = invoiceRepository.findByIdAndBusinessId(invoiceId, businessId)
                .orElseThrow(() -> new RuntimeException("Invoice not found or unauthorized"));

        // Guard: Check already-paid or void invoices
        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException("This invoice has already been paid in full.");
        }
        if (invoice.getStatus() == InvoiceStatus.VOID) {
            throw new IllegalStateException("Cannot record payment for a voided invoice.");
        }

        // 2. Create the Ledger Entry (Payment History)
        PaymentHistory payment = new PaymentHistory();
        payment.setInvoice(invoice);
        payment.setInvoiceId(invoice.getId());
        payment.setAmount(request.getAmount());
        payment.setMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "MANUAL");
        payment.setPaymentDate(LocalDate.now());
        
        // Save the payment receipt to the database
        paymentRepository.save(payment);

        // 3. Math Check & Status Update: Partial vs Full Payment
        BigDecimal totalPaidSoFar = paymentRepository.getTotalPaidForInvoice(invoice.getId());
        if (totalPaidSoFar == null) {
            totalPaidSoFar = payment.getAmountPaid();
        }

        BigDecimal invoiceTotal = invoice.getTotalAmount() != null ? invoice.getTotalAmount() : BigDecimal.ZERO;
        if (totalPaidSoFar.compareTo(invoiceTotal) >= 0) {
            invoice.setStatus(InvoiceStatus.PAID);
        } else {
            invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
        }
        
        // Save the updated invoice
        invoiceRepository.save(invoice);

        // Evict dashboard cache so UI instantly reflects new revenue
        if (cacheManager != null && cacheManager.getCache("dashboard_summary") != null) {
            cacheManager.getCache("dashboard_summary").evict(businessId);
        }
    }

    @Transactional
    public void recordPayment(String userEmail, UUID invoiceId, RecordPaymentRequest request) {
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        recordPayment(invoiceId, business.getId(), request);
    }

    public List<RecentInvoiceDTO> getAllInvoices(String userEmail) {
        Business business = businessRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("Business not found"));
        return getAllInvoices(business.getId());
    }

    public List<RecentInvoiceDTO> getAllInvoices(UUID businessId) {
        List<Invoice> invoices = invoiceRepository.findAllByBusinessIdOrderByIssueDateDesc(businessId);

        // Convert Entity list to DTO list
        return invoices.stream().map(inv -> RecentInvoiceDTO.builder()
                .id(inv.getId())
                .invoiceNumber(inv.getInvoiceNumber())
                .customerName(inv.getCustomer() != null ? inv.getCustomer().getName() : "Unknown")
                .totalAmount(inv.getTotalAmount())
                .status(inv.getStatus() != null ? inv.getStatus().name() : "")
                .build()
        ).toList();
    }
}
