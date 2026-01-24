package com.example.shopify.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class SendGridEmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${app.mail.from}")
    private String fromEmail;

    public void sendInvoiceEmail(String toEmail, String subject, String htmlBody, Path pdfPath) {
        try {
            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlBody);

            Mail mail = new Mail(from, subject, to, content);

            // 📎 PDF attachment
            byte[] pdfBytes = Files.readAllBytes(pdfPath);
            String encodedPdf = Base64.getEncoder().encodeToString(pdfBytes);

            Attachments attachment = new Attachments();
            attachment.setContent(encodedPdf);
            attachment.setType("application/pdf");
            attachment.setFilename(pdfPath.getFileName().toString());
            attachment.setDisposition("attachment");

            mail.addAttachments(attachment);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Email sent successfully via SendGrid API");
            } else {
                System.err.println("❌ SendGrid error: " + response.getStatusCode());
                System.err.println(response.getBody());
            }

        } catch (IOException e) {
            throw new RuntimeException("SendGrid email sending failed", e);
        }
    }
}
