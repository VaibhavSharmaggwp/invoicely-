package com.invoicely.backend.kafka;

import com.invoicely.backend.event.InvoiceCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class InvoiceProducer {
    // KafkaTemplate Spring ki taraf se ek tool hai jo messages send karne me madad karta hai
    private final KafkaTemplate<String, String> kafkaTemplate;
    // ObjectMapper Java objects ko JSON string mein convert karta hai
    private final ObjectMapper objectMapper;
    // Yeh hamare Kafka topic ka naam hai
    private static final String TOPIC = "invoice-events";

    public void sendInvoiceCreatedEvent(InvoiceCreatedEvent event){
        try{
            // 1. Java object ko JSON text mein badlo
            String eventJson = objectMapper.writeValueAsString(event);

            // Send to kafka server
            kafkaTemplate.send(TOPIC, eventJson);
            System.out.println("Producer: Invoice event sent to Kafka -> " + event.getInvoiceNumber());
        }catch (Exception e){
            System.err.println("Producer Error: Failed to send event to Kafka");
            e.printStackTrace();
        }
    }
}
