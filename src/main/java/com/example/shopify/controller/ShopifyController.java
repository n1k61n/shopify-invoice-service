package com.example.shopify.controller;

import com.example.shopify.service.InvoiceService;
import com.example.shopify.service.SendGridEmailService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class ShopifyController {

    private final InvoiceService invoiceService;
    private final SendGridEmailService sendGridEmailService;

    @PostMapping("/shopify/webhook/order-created")
    public ResponseEntity<String> handleOrder(@RequestBody JsonNode payload) {

        // 1️⃣ Order info
        String orderId = payload.path("id").asText();
        String email = payload.path("email").asText();

        Path pdfPath = Path.of("fakturalar/faktura_" + orderId + ".pdf");

        // 2️⃣ PDF yarat
        invoiceService.generatePdf(payload);

        // 3️⃣ Email (SendGrid API)
        sendGridEmailService.sendInvoiceEmail(
                email,
                "Your Invoice #" + orderId,
                "<h2>Thanks for your order</h2><p>Your invoice is attached.</p>",
                pdfPath
        );

        return ResponseEntity.ok("Success");
    }
}
