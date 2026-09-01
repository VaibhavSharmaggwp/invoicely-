package com.invoicely.backend.kafka;


import com.invoicely.backend.Service.PdfEmailService;
import com.invoicely.backend.event.InvoiceCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class NotificationConsumer {
    private final ObjectMapper objectMapper;
    private final PdfEmailService pdfEmailService;

    // @KafkaListener is method ko hamesha background me run karta rahega.
    // Jaise hi 'invoice-events' topic par koi naya message aayega, yeh function trigger ho jayega.
    @KafkaListener(topics = "invoice-events", groupId = "invoicely-notification-group")
    public void consumeInvoiceEvent(String eventJson){
        try{
            // 1. JSON text ko wapas Java Object mein badlo
            InvoiceCreatedEvent event = objectMapper.readValue(eventJson, InvoiceCreatedEvent.class);
            System.out.println("🔔 Kafka Consumer: Received event for " + event.getInvoiceNumber() + ". Generating PDF...");

            // Yahan hum apna naya email service call kar rahe hain!
            pdfEmailService.sendInvoiceEmail(
                    event.getCustomerEmail(),
                    event.getCustomerName(),
                    event.getInvoiceNumber(),
                    event.getTotalAmount().toString(),
                    event.getInvoiceId().toString()
            );

            // 2. Yahan hum actual Email ya WhatsApp API call karenge aage chal ke.
            // Abhi ke liye hum sirf console par print kar rahe hain.
            System.out.println("\n=========================================");
            System.out.println("🔔 Consumer (Background Task Started):");
            System.out.println("📧 Sending Email to: " + event.getCustomerEmail());
            System.out.println("📱 Sending WhatsApp reminder to: " + event.getCustomerName());
            System.out.println("💰 Amount Due: ₹" + event.getTotalAmount());
            System.out.println("=========================================\n");

            // Thread.sleep simulate kar raha hai ki Notification bhejne mein time lagta hai
            // Lekin kyu ki yeh alag thread me hai, API response slow nahi hoga!
            Thread.sleep(3000);
        }catch (Exception e){
            System.err.println("❌ Consumer Error: Failed to process event");
            e.printStackTrace();
        }
    }

    // Yeh naya listener sirf reminders wale topic ko sune-ga
    @KafkaListener(topics = "invoice-reminders", groupId = "invoicely-notification-group")
    public void consumeReminderEvent(String eventJson){
        try{
            // JSON string ko wapas PaymentReminderEvent object mein convert karo
            com.invoicely.backend.event.PaymentReminderEvent event =
                    objectMapper.readValue(eventJson, com.invoicely.backend.event.PaymentReminderEvent.class);
            System.out.println("Kafka Consumer: Picked up "
                    + event.getReminderType() + " reminder for " + event.getInvoiceNumber() + ". Sending email...");

            // Apni email service ko call karo
            pdfEmailService.sendReminderEmail(
                    event.getCustomerEmail(),
                    event.getCustomerName(),
                    event.getInvoiceNumber(),
                    event.getTotalAmount().toString(),
                    event.getInvoiceId().toString(),
                    event.getReminderType()
            );

        } catch (Exception e) {
            System.err.println("❌ Consumer Error: Failed to process reminder event");
        e.printStackTrace();
        }
    }
}
