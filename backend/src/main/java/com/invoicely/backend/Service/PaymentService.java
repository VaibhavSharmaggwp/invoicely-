package com.invoicely.backend.Service;

import com.invoicely.backend.dto.PaymentWebhookDTO;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.repository.InvoiceRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final InvoiceRepository invoiceRepository;
    private final CacheManager cacheManager; // Redis cache manually control karne ke liye

    @Transactional
    public void processPaymentWebhook(PaymentWebhookDTO webhookDTO) {
        // 1. Invoice find karo
        Invoice invoice = invoiceRepository.findById(webhookDTO.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found from webhook"));

        // 2. Agar payment successful hai, toh status update karo
        if("SUCCESS".equalsIgnoreCase(webhookDTO.getStatus())) {
            invoice.setStatus(InvoiceStatus.PAID);
            invoiceRepository.save(invoice);

            System.out.println("💰 PAYMENT RECEIVED! Invoice " + invoice.getInvoiceNumber() + " marked as PAID.");

            // 3. Redis Cache Eviction (Crucial Step)
            // Kyunki webhook Razorpay se aaya hai, hamare paas Spring Security ka user context nahi hai.
            // Hum invoice ki entity se business ka email nikalenge aur cache delete karenge.
            String businessEmail = invoice.getBusiness().getEmail();
            if(cacheManager.getCache("dashboard_summary") != null) {
                cacheManager.getCache("dashboard_summary").evict(businessEmail);
                System.out.println("REDIS: Cleared dashboard cache for " + businessEmail);
            }
        }
    }
}
