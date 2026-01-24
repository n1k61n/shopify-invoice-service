package com.example.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;

@Service
public class InvoiceService {

    private final String FOLDER_PATH = "invoices";

    public void generatePdf(JsonNode payload) {
        try {
            // Extract data from JSON
            String orderId = payload.path("id").asText();
            String customerName = payload.path("customer").path("first_name").asText() + " " +
                    payload.path("customer").path("last_name").asText();
            String totalPrice = payload.path("total_price").asText();
            String currency = payload.path("currency").asText("USD");

            // Ensure directory exists
            Path path = Paths.get(FOLDER_PATH);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // Build Table Rows
            StringBuilder itemsHtml = new StringBuilder();
            payload.path("line_items").forEach(item -> {
                itemsHtml.append("<tr>")
                        .append("<td style='padding: 10px; border-bottom: 1px solid #eee;'>").append(item.path("title").asText()).append("</td>")
                        .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: center;'>").append(item.path("quantity").asInt()).append("</td>")
                        .append("<td style='padding: 10px; border-bottom: 1px solid #eee; text-align: right;'>").append(item.path("price").asText()).append(" ").append(currency).append("</td>")
                        .append("</tr>");
            });

            // Base64 Logo (shortened for readability, keep your original string here)
            String logoBase64 = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";

            // Construct XHTML Content
            String htmlContent = "<!DOCTYPE html><html><body style='font-family: sans-serif; margin: 40px; color: #333;'>" +
                    "<div style='text-align: center; margin-bottom: 30px;'>" +
                    "   <img src='" + logoBase64 + "' style='width: 120px; height: auto;' />" +
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

            // Generate PDF
            String fileName = FOLDER_PATH + "/invoice_" + orderId + ".pdf";
            try (OutputStream os = new FileOutputStream(fileName)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(htmlContent, "/");
                builder.toStream(os);
                builder.run();
            }

            System.out.println("Success: Invoice saved to " + fileName);

        } catch (Exception e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}