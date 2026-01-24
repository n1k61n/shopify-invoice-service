package com.example.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.util.Base64;

@Service
public class InvoiceService {

    private final String FOLDER_PATH = "fakturalar";

    public void generatePdf(JsonNode payload) {
        try {
            String orderId = payload.path("id").asText();
            String customerName = payload.path("customer").path("first_name").asText() + " " + payload.path("customer").path("last_name").asText();
            String totalPrice = payload.path("total_price").asText();

            StringBuilder itemsHtml = new StringBuilder();
            payload.path("line_items").forEach(item -> {
                itemsHtml.append("<tr>")
                        .append("<td style='padding: 8px; border-bottom: 1px solid #ddd;'>").append(item.path("title").asText()).append("</td>")
                        .append("<td style='padding: 8px; border-bottom: 1px solid #ddd;'>").append(item.path("quantity").asInt()).append("</td>")
                        .append("<td style='padding: 8px; border-bottom: 1px solid #ddd; text-align: right;'>").append(item.path("price").asText()).append(" USD</td>")
                        .append("</tr>");
            });

            // Loqonu təhlükəsiz yükləyirik
            String logoBase64 = convertImageToBase64("src/main/resources/static/images/logo.png");
            String logoHtml = logoBase64.isEmpty() ? "" : "<img src='data:image/png;base64," + logoBase64 + "' width='100' />";

            String html = "<html><body style='font-family: Arial, sans-serif; padding: 20px;'>" +
                    "<div style='display: block; margin-bottom: 20px;'>" +
                    logoHtml +
                    "<h1 style='color: #008060; float: right;'>OFFICIAL INVOICE</h1>" +
                    "<div style='clear: both;'></div>" +
                    "</div>" +
                    "<p><strong>Customer:</strong> " + customerName + "</p>" + // [cite: 1]
                    "<p><strong>Invoice No:</strong> #" + orderId + "</p>" + // [cite: 2]
                    "<table style='width: 100%; border-collapse: collapse; margin-top: 20px;'>" +
                    "<tr style='background: #f4f6f8;'>" +
                    "<th style='text-align: left; padding: 10px;'>Product</th>" +
                    "<th style='text-align: left; padding: 10px;'>Quantity</th>" +
                    "<th style='text-align: right; padding: 10px;'>Price</th>" +
                    "</tr>" +
                    itemsHtml.toString() + // [cite: 4]
                    "</table>" +
                    "<div style='margin-top: 30px; text-align: right; font-size: 20px;'>" +
                    "<strong>Total: " + totalPrice + " USD</strong>" + // [cite: 5]
                    "</div>" +
                    "<hr style='margin-top: 50px; border: 0; border-top: 1px solid #eee;' />" +
                    "<div style='margin-top: 20px; text-align: center; color: #777; font-size: 12px;'>" +
                    "<p>Thank you for your business!</p>" +
                    "<p>SmartInvoice Inc. | Baku, Azerbaijan | support@smartinvoice.com</p>" +
                    "</div>" +
                    "</body></html>";

            Files.createDirectories(Paths.get(FOLDER_PATH));
            File file = new File(FOLDER_PATH + File.separator + "faktura_" + orderId + ".pdf");

            try (OutputStream os = new FileOutputStream(file)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, null);
                builder.toStream(os);
                builder.run();
            }
            System.out.println("✅ Faktura uğurla yaradıldı: " + file.getName());

        } catch (Exception e) {
            System.err.println("❌ PDF yaradılarkən ciddi xəta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String convertImageToBase64(String imagePath) {
        try {
            byte[] fileContent = Files.readAllBytes(Paths.get(imagePath));
            return Base64.getEncoder().encodeToString(fileContent);
        } catch (IOException e) {
            System.err.println("❌ Loqo faylı tapılmadı: " + imagePath);
            return "";
        }
    }
}