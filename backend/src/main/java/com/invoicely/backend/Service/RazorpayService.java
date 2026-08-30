package com.invoicely.backend.Service;

import com.invoicely.backend.entity.Invoice;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.ZoneId;

@Service
public class RazorpayService {

    @Value("${app.razorpay.key-id}")
    private String keyId;

    @Value("${app.razorpay.key-secret}")
    private String keySecret;

    public String createPaymentLink(Invoice invoice){
        try{
            // 1. Initialize Razorpay Client
            RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);

            // 2. Prepare the payload
            JSONObject paymentLinkRequest = new JSONObject();

            // Razorpay strictly accepts amount in PAISE, not Rupees.
            BigDecimal amountInPaise = invoice.getTotalAmount().multiply(new BigDecimal("100"));
            paymentLinkRequest.put("amount", amountInPaise.longValue());
            paymentLinkRequest.put("currency", "INR");
            paymentLinkRequest.put("accept_partial", false);
            paymentLinkRequest.put("description", "Payment for Invoice: " + invoice.getInvoiceNumber());

            // Expiry date setup (Due date at 11:59 PM)
            long expireBy = invoice.getDueDate().atTime(23, 59)
                    .atZone(ZoneId.systemDefault()).toEpochSecond();
            paymentLinkRequest.put("expire_by", expireBy);

            // Add customer details
            JSONObject customer = new JSONObject();
            customer.put("name", invoice.getCustomer().getName());
            customer.put("email", invoice.getCustomer().getEmail());
            customer.put("contact", invoice.getCustomer().getPhone());
            paymentLinkRequest.put("customer", customer);

            // Notes allow us to pass custom data. We pass invoiceId so we know which invoice was paid!
            JSONObject notes = new JSONObject();
            notes.put("invoiceId", invoice.getId().toString());
            notes.put("invoice_id", invoice.getId().toString());
            paymentLinkRequest.put("notes", notes);

            // 3. Make the API Call to Razorpay
            PaymentLink paymentLink = razorpayClient.paymentLink.create(paymentLinkRequest);

            // 4. Return the short URL (e.g., https://rzp.io/i/abcd123)
            return paymentLink.get("short_url").toString();

        }catch (Exception e){
            System.err.println("❌ Failed to create Razorpay Payment Link: " + e.getMessage());
            return null;
        }
    }
}
