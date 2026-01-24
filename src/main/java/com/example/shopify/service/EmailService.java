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

    @Value("${spring.mail.username}")
    private String fromEmail;



    public void sendInvoiceWithAttachment(String toEmail, String orderId, String filePath) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Sizin Fakturanız - Sifariş #" + orderId);
            helper.setText("Salam, alış-veriş etdiyiniz üçün təşəkkür edirik! Fakturanız əlavədədir.");

            // PDF faylını əlavə edirik
            FileSystemResource file = new FileSystemResource(new File(filePath));
            helper.addAttachment("Faktura_" + orderId + ".pdf", file);

            mailSender.send(message);
            System.out.println("📧 Email uğurla göndərildi: " + toEmail);
        } catch (Exception e) {
            System.err.println("Email göndərilərkən xəta: " + e.getMessage());
        }
    }


}