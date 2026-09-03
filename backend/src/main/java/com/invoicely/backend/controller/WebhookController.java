package com.invoicely.backend.controller;

import com.invoicely.backend.Service.PaymentService;
import com.invoicely.backend.dto.PaymentWebhookDTO;
import com.razorpay.Utils;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final PaymentService paymentService;

    @Value("${app.razorpay.webhook-secret:invoicely_secret_123}")
    private String webhookSecret;

    @PostMapping("/razorpay")
    public ResponseEntity<String> handleRazorpayWebhook(
            @RequestBody String rawPayload,
            @RequestHeader(value = "x-razorpay-signature", required = false) String signature) {

        try {
            // 1. Verify the signature (Security Check)
            if (signature == null || !Utils.verifyWebhookSignature(rawPayload, signature, webhookSecret)) {
                System.out.println("🚨 INTRUSION ATTEMPT: Invalid Webhook Signature!");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Webhook Signature!");
            }

            // 2. Parse JSON payload from Razorpay
            JSONObject jsonPayload = new JSONObject(rawPayload);
            String event = jsonPayload.optString("event");

            System.out.println("✅ Signature Verified. Received Razorpay Event: " + event);

            // 3. Only process if the payment was actually captured
            if ("payment.captured".equals(event)) {
                JSONObject paymentEntity = jsonPayload.getJSONObject("payload")
                                                      .getJSONObject("payment")
                                                      .getJSONObject("entity");
                
                // Extract the notes we sent when creating the link
                JSONObject notes = paymentEntity.getJSONObject("notes");
                String invoiceIdStr = notes.has("invoice_id") ? notes.getString("invoice_id") : notes.getString("invoiceId");
                
                // 🚀 NEW: Extract the amount and convert Paise to Rupees
                long amountInPaise = paymentEntity.getLong("amount");
                java.math.BigDecimal amountInRupees = new java.math.BigDecimal(amountInPaise)
                                                            .divide(new java.math.BigDecimal("100"));
                
                // 4. Map it to your DTO
                PaymentWebhookDTO dto = new PaymentWebhookDTO();
                dto.setInvoiceId(UUID.fromString(invoiceIdStr));
                dto.setPaymentId(paymentEntity.getString("id"));
                dto.setStatus("SUCCESS"); 
                dto.setAmountPaid(amountInRupees); // <-- Pass the correct amount here!

                // 5. Hand it off to your perfectly agnostic PaymentService
                paymentService.processPaymentWebhook(dto);
            }

            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            System.err.println("Webhook processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook Error: " + e.getMessage());
        }
    }
}
