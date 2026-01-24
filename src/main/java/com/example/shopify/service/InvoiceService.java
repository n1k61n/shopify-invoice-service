package com.example.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;

@Service
public class InvoiceService {

    private final String FOLDER_PATH = "invoices";

    public String generatePdf(JsonNode payload) {
        try {
            // 1. Məlumatların JSON-dan oxunması
            String orderId = payload.path("id").asText();
            String customerName = payload.path("customer").path("first_name").asText() + " " +
                    payload.path("customer").path("last_name").asText();
            String totalPrice = payload.path("total_price").asText();
            String currency = payload.path("currency").asText("USD");

            // 2. Qovluğun yaradılması
            Path path = Paths.get(FOLDER_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // 3. Cədvəl sətirlərinin yaradılması
            StringBuilder itemsHtml = new StringBuilder();
            payload.path("line_items").forEach(item -> {
                itemsHtml.append("<tr>")
                        .append("<td style='padding: 10px; border-bottom: 1px solid #eee;'>").append(item.path("title").asText()).append("</td>")
                        .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: center;'>").append(item.path("quantity").asInt()).append("</td>")
                        .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: right;'>").append(item.path("price").asText()).append(" ").append(currency).append("</td>")
                        .append("</tr>");
            });

            // 4. LOQO FIX: Base64 stringindəki boşluqları və sətir sonlarını təmizləyirik
            String logoBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
            String cleanLogo = logoBase64.replaceAll("\\s", "");

            // 5. XHTML (Strict format)
            String htmlContent = "<!DOCTYPE html><html><body style='font-family: sans-serif; margin: 40px; color: #333;'>" +
                    "<div style='text-align: center; margin-bottom: 30px;'>" +
                    "   <img src='" + cleanLogo + "' style='width: 120px; height: auto;' />" +
                    "</div>" +
                    "<div style='border-bottom: 2px solid #444; padding-bottom: 10px; margin-bottom: 20px;'>" +
                    "   <h1 style='margin: 0;'>INVOICE</h1>" +
                    "   <p style='margin: 5px 0;'>Order ID: #" + orderId + "</p>" +
                    "</div>" +
                    "<p><strong>Customer:</strong> " + customerName + "</p>" +
                    "<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>" +
                    "   <thead>" +
                    "       <tr style='background-color: #f8f8f8;'>" +
                    "           <th style='text-align: left; padding: 10px; border-bottom: 2px solid #ddd;'>Product</th>" +
                    "           <th style='text-align: center; padding: 10px; border-bottom: 2px solid #ddd;'>Qty</th>" +
                    "           <th style='text-align: right; padding: 10px; border-bottom: 2px solid #ddd;'>Price</th>" +
                    "       </tr>" +
                    "   </thead>" +
                    "   <tbody>" + itemsHtml.toString() + "</tbody>" +
                    "</table>" +
                    "<div style='text-align: right; margin-top: 30px; font-size: 1.2em;'>" +
                    "   <strong>Total Amount: " + totalPrice + " " + currency + "</strong>" +
                    "</div>" +
                    "<div style='margin-top: 50px; font-size: 0.8em; color: #777; text-align: center;'>" +
                    "   <p>Thank you for your business!</p>" +
                    "</div>" +
                    "</body></html>";

            // 6. PDF-in generasiyası
            String fileName = FOLDER_PATH + "/invoice_" + orderId + ".pdf";
            try (OutputStream os = new FileOutputStream(fileName)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlContent, "/"); // "/" Base URL loqo üçün vacibdir
                builder.toStream(os);
                builder.run();
            }

            System.out.println("Success: Invoice saved to " + fileName);
            return fileName;

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}