package com.invoicely.backend.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfEmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username:vs9736400462@gmail.com}")
    private String fromEmail;

    public void sendInvoiceEmail(String toEmail, String customerName, String invoiceNumber,
                                 String totalAmount, String invoiceId) {

        try {
            // 1. Thymeleaf template ke andar variables set karna
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("invoiceNumber", invoiceNumber);
            context.setVariable("totalAmount", totalAmount);

            // 2. HTML template ko process karke ek final HTML String banana
            String htmlContent = templateEngine.process("invoice-template", context);

            // 3. HTML string ko PDF byte array me convert karna (Flying Saucer)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            // 4. MimeMessage setup karna
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Invoice " + invoiceNumber + " is ready");

            String paymentLink = "http://localhost:8080/api/v1/public/invoices/" + invoiceId;

            helper.setText("Hi " + customerName +
                    ",\n\nPlease find your invoice attached as a PDF." +
                    "\n\nYou can view the full details and pay securely here:\n"
                    + paymentLink + "\n\nThank you!", false);

            // 5. PDF ko as an attachment add karna
            helper.addAttachment(invoiceNumber + ".pdf", new ByteArrayResource(pdfBytes));

            // 6. Send email
            mailSender.send(message);
            System.out.println("PDF Email successfully sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send PDF email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to send text-based reminder emails for UPCOMING & OVERDUE invoices
    public void sendReminderEmail(String toEmail, String customerName, String invoiceNumber,
                                  String totalAmount, String invoiceId, String reminderType) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            String paymentLink = "http://localhost:8080/api/v1/public/invoices/" + invoiceId;

            // Type ke hisaab se subject aur message badal do
            if ("UPCOMING".equals(reminderType)) {
                helper.setSubject("Reminder: Invoice " + invoiceNumber + " is due soon");
                helper.setText("Hi " + customerName
                        + ",\n\nJust a gentle reminder that your invoice of ₹" + totalAmount +
                        " is due in 3 days.\n\nYou can pay securely here:\n" + paymentLink + "\n\nThank you!");
            } else if ("OVERDUE".equals(reminderType)) {
                helper.setSubject("URGENT: Invoice " + invoiceNumber + " is OVERDUE");
                helper.setText("Hi " + customerName + ",\n\nThis is an automated notice that your invoice of ₹"
                        + totalAmount + " is now OVERDUE.\n\nPlease clear the payment immediately using this link:\n"
                        + paymentLink + "\n\nThank you!");
            }

            mailSender.send(message);
            System.out.println("📧 " + reminderType + " Reminder sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send reminder email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
