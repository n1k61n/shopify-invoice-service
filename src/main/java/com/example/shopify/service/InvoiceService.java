package com.example.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class InvoiceService {

    public Path generatePdf(JsonNode payload) {
        try {
            String orderId = payload.path("id").asText();
            String email = payload.path("email").asText();
            String totalPrice = payload.path("total_price").asText();
            String totalTax = payload.path("total_tax").asText("0.00");
            String financialStatus = payload.path("financial_status").asText("pending");
            String currency = payload.path("currency").asText("USD");

            // Müştəri adı
            JsonNode customer = payload.path("customer");
            String firstName = customer.path("first_name").asText("");
            String lastName = customer.path("last_name").asText("");
            String customerName = (firstName + " " + lastName).trim();
            if (customerName.isEmpty()) customerName = "N/A";

            // Ünvan
            JsonNode shipping = payload.path("shipping_address");
            String address = shipping.path("address1").asText("") + ", " +
                    shipping.path("city").asText("") + ", " +
                    shipping.path("country").asText("");

            // Tarix
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));

            // Ödəniş statusu
            String paymentStatus = financialStatus.equalsIgnoreCase("paid") ? "✔ PAID" : "⏳ UNPAID";

            Path dir = Path.of("invoices");
            Files.createDirectories(dir);
            Path pdfPath = dir.resolve("invoice_" + orderId + ".pdf");

            PdfWriter writer = new PdfWriter(pdfPath.toString());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // ── Başlıq ──
            document.add(new Paragraph("OFFICIAL INVOICE")
                    .setBold()
                    .setFontSize(22)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.DARK_GRAY));

            document.add(new Paragraph("SmartInvoice Inc. | Baku, Azerbaijan | support@smartinvoice.com")
                    .setFontSize(9)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.add(new Paragraph(" "));

            // ── Faktura məlumatları ──
            document.add(new Paragraph("Invoice No: #" + orderId).setBold());
            document.add(new Paragraph("Date: " + date));
            document.add(new Paragraph("Payment Status: " + paymentStatus).setBold());
            document.add(new Paragraph(" "));

            // ── Müştəri məlumatları ──
            document.add(new Paragraph("Customer Information").setBold().setFontSize(12));
            document.add(new Paragraph("Name: " + customerName));
            document.add(new Paragraph("Email: " + email));
            document.add(new Paragraph("Address: " + address));
            document.add(new Paragraph(" "));

            // ── Məhsullar cədvəli ──
            document.add(new Paragraph("Order Items").setBold().setFontSize(12));

            Table table = new Table(UnitValue.createPercentArray(new float[]{50, 20, 30}))
                    .useAllAvailableWidth();

            // Cədvəl başlığı
            table.addHeaderCell(headerCell("Product"));
            table.addHeaderCell(headerCell("Quantity"));
            table.addHeaderCell(headerCell("Price (" + currency + ")"));

            // Məhsullar
            for (JsonNode item : payload.path("line_items")) {
                String title = item.path("title").asText();
                int qty = item.path("quantity").asInt();
                String price = item.path("price").asText();

                table.addCell(new Cell().add(new Paragraph(title)).setBorder(new SolidBorder(0.5f)));
                table.addCell(new Cell().add(new Paragraph(String.valueOf(qty))).setBorder(new SolidBorder(0.5f)));
                table.addCell(new Cell().add(new Paragraph(price)).setBorder(new SolidBorder(0.5f)));
            }

            document.add(table);
            document.add(new Paragraph(" "));

            // ── Cəmi ──
            document.add(new Paragraph("Tax: " + totalTax + " " + currency)
                    .setTextAlignment(TextAlignment.RIGHT));
            document.add(new Paragraph("TOTAL: " + totalPrice + " " + currency)
                    .setBold()
                    .setFontSize(14)
                    .setTextAlignment(TextAlignment.RIGHT));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Thank you for your business!")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontColor(ColorConstants.GRAY));

            document.close();
            return pdfPath;

        } catch (IOException e) {
            throw new RuntimeException("PDF creation failed", e);
        }
    }

    private Cell headerCell(String text) {
        return new Cell()
                .add(new Paragraph(text).setBold())
                .setBackgroundColor(ColorConstants.LIGHT_GRAY)
                .setBorder(new SolidBorder(1f));
    }
}