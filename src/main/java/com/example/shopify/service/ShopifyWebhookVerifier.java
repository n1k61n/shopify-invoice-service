package com.example.shopify.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class ShopifyWebhookVerifier {

    @Value("${shopify.webhook.secret}")
    private String webhookSecret;

    public boolean verify(String rawBody, String hmacHeader) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(webhookSecret.getBytes(), "HmacSHA256");

            mac.init(secretKey);
            byte[] digest = mac.doFinal(rawBody.getBytes());

            String calculatedHmac =
                    Base64.getEncoder().encodeToString(digest);

            return calculatedHmac.equals(hmacHeader);

        } catch (Exception e) {
            return false;
        }
    }
}

