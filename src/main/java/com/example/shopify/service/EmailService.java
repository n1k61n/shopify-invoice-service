package com.example.shopify.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import java.io.File;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;



    public void sendInvoiceWithAttachment(String toEmail, String orderId, String filePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); // UTF-8 əlavə edildi

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Your Invoice - Order #" + orderId);
            helper.setText("Hello, thank you for your purchase! Please find your invoice attached below.", true);

            FileSystemResource file = new FileSystemResource(new File(filePath));
            if (file.exists()) {
                helper.addAttachment("Invoice_" + orderId + ".pdf", file);
            }

            mailSender.send(message);
            System.out.println("📧 Email successfully sent to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace(); // Xətanın tam detallarını görmək üçün
        }
    }


}