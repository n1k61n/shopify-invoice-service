package com.example.shopify.controller;

import com.example.shopify.service.InvoiceService;
import com.example.shopify.service.SendGridEmailService;
import com.example.shopify.service.ShopifyWebhookVerifier;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
public class ShopifyController {

    private final InvoiceService invoiceService;
    private final SendGridEmailService sendGridEmailService;
    private final ShopifyWebhookVerifier shopifyWebhookVerifier;
    private final ObjectMapper objectMapper;

    @PostMapping("/shopify/webhook/order-created")
    public ResponseEntity<String> handleOrder(
            @RequestHeader(value = "X-Shopify-Hmac-Sha256", required = false) String hmacHeader,
            @RequestBody String rawBody
    ) {
        // 0) Header yoxdursa
        if (hmacHeader == null || hmacHeader.isBlank()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Missing signature header");
        }

        // 1) İmza yoxlaması
        if (!shopifyWebhookVerifier.verify(rawBody, hmacHeader)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid signature");
        }

        Path pdf = null;
        try {
            // 2) JSON parse
            JsonNode payload = objectMapper.readTree(rawBody);

            // 3) Email çıxart (fallback ilə)
            String toEmail = extractEmail(payload);
            if (toEmail == null || toEmail.isBlank()) {
                return ResponseEntity.badRequest().body("Order email not found in payload");
            }

            // (opsional) 4) Order id al (idempotency üçün lazımdır)
            String orderId = payload.path("id").asText(null);

            // 5) PDF yarat
            pdf = invoiceService.generatePdf(payload);

            // 6) Email göndər
            sendGridEmailService.sendInvoiceEmail(
                    toEmail,
                    "Your Invoice" + (orderId != null ? " (#" + orderId + ")" : ""),
                    "<h2>Thanks for your order</h2><p>Invoice attached.</p>",
                    pdf
            );

            return ResponseEntity.ok("Webhook processed");

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            return ResponseEntity.badRequest().body("Invalid JSON");
        } catch (Exception e) {
            // burada istəsən log da at: log.error("Webhook failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Processing failed");
        } finally {
            // 7) temp faylı sil (invoiceService temp yaradırsa)
            safeDelete(pdf);
        }
    }

    private String extractEmail(JsonNode payload) {
        // Shopify order payload-da email bəzən buradadır:
        // 1) payload.email
        String email = payload.path("email").asText("");
        if (!email.isBlank()) return email;

        // 2) payload.customer.email
        email = payload.path("customer").path("email").asText("");
        if (!email.isBlank()) return email;

        // 3) payload.contact_email (bəzən olur)
        email = payload.path("contact_email").asText("");
        if (!email.isBlank()) return email;

        return null;
    }

    private void safeDelete(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }
}