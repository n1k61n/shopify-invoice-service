package com.example.shopify.controller;


import com.example.shopify.service.EmailService;
import com.example.shopify.service.InvoiceService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ShopifyController {


    private final InvoiceService invoiceService;
    private final EmailService emailService;




    @PostMapping("/shopify/webhook/order-created")
    public ResponseEntity<String> handleOrder(@RequestBody JsonNode payload) {
        // 1. PDF-i yaradırıq
        String orderId = payload.path("id").asText();
        String email = payload.path("email").asText();
        String filePath = "fakturalar/faktura_" + orderId + ".pdf";

        invoiceService.generatePdf(payload);

        // 2. Email-i göndəririk
        emailService.sendInvoiceWithAttachment(email, orderId, filePath);

        return ResponseEntity.ok("Success");
    }
}