package com.example.shopify.controller;

import com.example.shopify.service.InvoiceService;
import com.example.shopify.service.SendGridEmailService;
import com.example.shopify.service.ShopifyWebhookVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class ShopifyController {

    private final InvoiceService invoiceService;
    private final SendGridEmailService sendGridEmailService;
    private final ShopifyWebhookVerifier shopifyWebhookVerifier;

    @PostMapping("/shopify/webhook/order-created")
    public ResponseEntity<String> handleOrder(
            @RequestHeader("X-Shopify-Hmac-Sha256") String hmacHeader,
            @RequestBody String rawBody
    ) {
        if (!shopifyWebhookVerifier.verify(rawBody, hmacHeader)) {
            return ResponseEntity.status(403).body("Invalid webhook signature");
        }

        // JSON-u SONRADAN parse edirik
        JsonNode payload;
        try {
            payload = new ObjectMapper().readTree(rawBody);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid JSON");
        }

        // PDF + Email
        Path pdf = invoiceService.generatePdf(payload);

        sendGridEmailService.sendInvoiceEmail(
                payload.path("email").asText(),
                "Your Invoice",
                "<h2>Thanks for your order</h2>",
                pdf
        );
        return ResponseEntity.ok("Webhook processed");
    }

}
