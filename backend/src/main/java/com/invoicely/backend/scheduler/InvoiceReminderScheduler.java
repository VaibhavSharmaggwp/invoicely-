package com.invoicely.backend.scheduler;


import com.invoicely.backend.entity.Invoice;
import com.invoicely.backend.enums.InvoiceStatus;
import com.invoicely.backend.event.InvoiceCreatedEvent;
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

    // Cron expression: "Sekend Minute Ghanta Din Mahina Din_ka_naam"
    // "0 0 8 * * *" = Har subah 8:00 AM baje chalega
    // Abhi test karne ke liye hum isko har 1 minute mein chalayenge: "0 * * * * *"
    @Scheduled(cron = "0 * * * * *")
    public void scanAndSendReminders(){
        System.out.println("⏰ Scheduler Woke Up: Checking for overdue invoices...");
        // 1. Aaj ki date nikalo
        LocalDate today = LocalDate.now();

        // 2. Wo saari invoices nikalo jo ISSUED hain (matlab abhi tak PAID nahi hui)
        // aur jinki due date aaj ya aaj se pehle ki hai
        List<Invoice> dueInvoice = invoiceRepository.findByStatusAndDueDateLessThanEqual(InvoiceStatus.ISSUED, today);

        if(dueInvoice.isEmpty()){
            System.out.println("✅ Koi due invoice nahi hai. Going back to sleep.");
            return;
        }
        // 3. Har due invoice ke liye ek Kafka event generate karo
        for(Invoice invoice: dueInvoice){
            // Note: Real app mein hum ek naya 'InvoiceOverdueEvent' banayenge.
            // TODO ->Abhi testing ke liye hum purana wala event hi reuse kar rahe hain taaki Consumer ko change na karna pade.
            InvoiceCreatedEvent reminderEvent = InvoiceCreatedEvent.builder()
                    .invoiceId(invoice.getId())
                    .invoiceNumber(invoice.getInvoiceNumber() + " [REMINDER]")
                    .customerEmail(invoice.getCustomer().getEmail())
                    .customerName(invoice.getCustomer().getName())
                    .totalAmount(invoice.getTotalAmount())
                    .build();

            // 4. Chef (Producer) ko bolo ki ticket counter par rakh de
            invoiceProducer.sendInvoiceCreatedEvent(reminderEvent);
        }
    }
}
