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
            @RequestHeader("x-razorpay-signature") String signature){
        try{
            // 1. Verify the signature using Razorpay's SDK
            boolean isValid = Utils.verifySignature(rawPayload, signature, webhookSecret);

            if(!isValid){
                System.out.println("🚨 INTRUSION ATTEMPT: Invalid Webhook Signature!");
                return ResponseEntity.badRequest().body("Invalid Webhook Signature!");
            }

            // 2. Parse JSON payload from Razorpay using org.json.JSONObject
            JSONObject root = new JSONObject(rawPayload);
            String event = root.optString("event");

            System.out.println("✅ Signature Verified. Received Razorpay Event: " + event);

            if ("payment.captured".equals(event) || "order.paid".equals(event) || "payment_link.paid".equals(event)) {
                JSONObject payload = root.optJSONObject("payload");
                if (payload != null) {
                    JSONObject entity = null;
                    if (payload.has("payment_link")) {
                        entity = payload.getJSONObject("payment_link").getJSONObject("entity");
                    } else if (payload.has("payment")) {
                        entity = payload.getJSONObject("payment").getJSONObject("entity");
                    } else if (payload.has("order")) {
                        entity = payload.getJSONObject("order").getJSONObject("entity");
                    }

                    if (entity != null) {
                        String paymentId = entity.optString("id");
                        String status = "SUCCESS";

                        JSONObject notes = entity.optJSONObject("notes");
                        String invoiceIdStr = notes != null ? notes.optString("invoiceId", notes.optString("invoice_id", null)) : null;

                        if (invoiceIdStr != null && !invoiceIdStr.isEmpty() && !"null".equalsIgnoreCase(invoiceIdStr)) {
                            PaymentWebhookDTO webhookDTO = new PaymentWebhookDTO();
                            webhookDTO.setInvoiceId(UUID.fromString(invoiceIdStr));
                            webhookDTO.setPaymentId(paymentId);
                            webhookDTO.setStatus(status);

                            paymentService.processPaymentWebhook(webhookDTO);
                        } else {
                            System.out.println("⚠️ Webhook received but no invoiceId found in entity notes.");
                        }
                    }
                }
            }

            return ResponseEntity.ok("Webhook processed successfully");
        }catch (Exception e){
            System.err.println("Webhook processing error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Webhook Error: " + e.getMessage());
        }
    }
}
