package com.invoicely.backend.Service;

import com.invoicely.backend.dto.PaymentWebhookDTO;
import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.entity.PaymentHistory;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.repository.InvoiceRepository;
import com.invoicely.backend.repository.PaymentHistoryRepository;
import com.invoicely.backend.repository.PaymentRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final InvoiceRepository invoiceRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final CacheManager cacheManager; // Redis cache manually control karne ke liye

    @Transactional
    public void processPaymentWebhook(PaymentWebhookDTO webhookDTO) {
        // 1. Invoice find karo
        Invoice invoice = invoiceRepository.findById(webhookDTO.getInvoiceId())
                .orElseThrow(() -> new RuntimeException("Invoice not found from webhook"));

        // 2. Agar payment successful hai, toh status update karo
        if("SUCCESS".equalsIgnoreCase(webhookDTO.getStatus())) {
            // 1. Ledger me record save karo
            PaymentHistory history = PaymentHistory.builder()
                            .invoice(invoice)
                            .amountPaid(webhookDTO.getAmountPaid())
                                    .paymentMethod("RAZORPAY")
                                            .transactionId(webhookDTO.getPaymentId())
                                                    .build();
            paymentHistoryRepository.save(history);

            // 2. Total paid amount calculate karo
            BigDecimal totalPaidSoFar = paymentHistoryRepository.getTotalPaidForInvoice(invoice.getId());
            // If totalPaidSoFar is somehow null (e.g., first payment failed to save), default to the current payment
            if(totalPaidSoFar == null){
                totalPaidSoFar = webhookDTO.getAmountPaid();
            }

            // 3. Status decide karo: Poora paisa aa gaya ya partial hai?
            // compareTo() returns 0 if equal, -1 if less, 1 if greater
            if(totalPaidSoFar.compareTo(invoice.getTotalAmount()) >= 0){
                invoice.setStatus(InvoiceStatus.PAID);
                System.out.println("FULL PAYMENT! Invoice marked as PAID");
            }else{
                invoice.setStatus(InvoiceStatus.PARTIALLY_PAID);
                System.out.println("⏳ PARTIAL PAYMENT! Received ₹"
                        + webhookDTO.getAmountPaid() + ". Status: PARTIALLY_PAID.");
            }

            invoiceRepository.save(invoice);

            // 4. Cache clear karo taaki dashboard update ho jaye

            String businessEmail = invoice.getBusiness().getEmail();
            if(cacheManager.getCache("dashboard_summary") != null) {
                cacheManager.getCache("dashboard_summary").evict(businessEmail);
                System.out.println("REDIS: Cleared dashboard cache for " + businessEmail);
            }
        }
    }
}
