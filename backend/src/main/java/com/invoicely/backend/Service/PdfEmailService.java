package com.invoicely.backend.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.context.Context;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;



import java.io.ByteArrayOutputStream;

@Service
@RequiredArgsConstructor
public class PdfEmailService {
    private final JavaMailSender MailSender;
    private final TemplateEngine TemplateEngine;
    private final TemplateEngine templateEngine;


    public void sendInvoiceEmail(String toEmail, String customerName, String invoiceNumber,
                                 String totalAmount, String invoiceId){


        try{
            // 1. Thymeleaf template ke andar variables set karna
            Context context = new Context();
            context.setVariable("customerName", customerName);
            context.setVariable("invoiceNumber", invoiceNumber);
            context.setVariable("totalAmount", totalAmount);

            // 2. HTML template ko process karke ek final HTML String banana
            String htmlContent = templateEngine.process("invoice-template", context);

            // 3. HTML string ko PDF byte array me convert karna (Flying Saucer Magic)
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlContent);
            renderer.layout();
            renderer.createPDF(outputStream);
            byte[] pdfBytes = outputStream.toByteArray();

            // 4. MimeMessage setup karna (Taaki hum attachment bhej sakein)
            MimeMessage message = MailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your Invoice " + invoiceNumber + " is ready");

            // Note: In real production, frontend URL use hoga. Abhi hum public API test link bhej rahe hain.
            String paymentLink = "http://localhost:8080/api/v1/public/invoices/" + invoiceId;

            helper.setText("Hi " + customerName +
                    ",\n\nPlease find your invoice attached as a PDF. " +
                    "\n\nYou can view the full details and pay securely here: "
                    + paymentLink + "\n\nThank you!", false);

            // 5. PDF ko as an attachment add karna
            helper.addAttachment(invoiceNumber + ".pdf", new ByteArrayResource(pdfBytes));

            // 6 send email
            MailSender.send(message);
            System.out.println("✅ PDF Email successfully sent to: " + toEmail);


        }catch (Exception e){
            System.err.println("❌ Failed to send PDF email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
