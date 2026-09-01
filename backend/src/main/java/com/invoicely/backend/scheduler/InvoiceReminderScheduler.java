package com.invoicely.backend.scheduler;

import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.event.PaymentReminderEvent;
import com.invoicely.backend.kafka.InvoiceProducer;
import com.invoicely.backend.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoiceReminderScheduler {

    private final InvoiceRepository invoiceRepository;
    private final InvoiceProducer invoiceProducer;

    // Real world mein hum isko subah 8 baje chalayenge: cron = "0 0 8 * * *"
    // Abhi test karne ke liye har 1 minute mein chalayenge: "0 * * * * *"
    @Scheduled(cron = "0 * * * * *")
    @org.springframework.transaction.annotation.Transactional 
    public void scanAndSendReminders() {
        System.out.println("🤖 Cron Job Woke Up: Scanning for payments...");

        LocalDate today = LocalDate.now();
        LocalDate threeDaysFromNow = today.plusDays(3);
        LocalDate yesterday = today.minusDays(1);

        // 1. UPCOMING REMINDERS: Jo invoices 3 din baad due hone wali hain
        List<Invoice> upcomingInvoices = invoiceRepository.findByStatusAndDueDate(InvoiceStatus.ISSUED, threeDaysFromNow);
        
        for (Invoice invoice : upcomingInvoices) {
            sendToKafka(invoice, "UPCOMING");
        }

        // 2. OVERDUE REMINDERS: Jo invoices kal due thi par abhi tak pay nahi hui
        List<Invoice> overdueInvoices = invoiceRepository.findByStatusAndDueDate(InvoiceStatus.ISSUED, yesterday);
        
        for (Invoice invoice : overdueInvoices) {
            sendToKafka(invoice, "OVERDUE");
        }
    }

    private void sendToKafka(Invoice invoice, String type) {
        PaymentReminderEvent event = PaymentReminderEvent.builder()
                .invoiceId(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerEmail(invoice.getCustomer().getEmail())
                .customerName(invoice.getCustomer().getName())
                .totalAmount(invoice.getTotalAmount())
                .reminderType(type)
                .build();

        invoiceProducer.sendReminderEvent(event);
    }
}
