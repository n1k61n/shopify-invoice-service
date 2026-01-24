package com.example.shopify.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class InvoiceService {

    public Path generatePdf(JsonNode payload) {
        try {
            String orderId = payload.path("id").asText();
            String email = payload.path("email").asText();
            String totalPrice = payload.path("total_price").asText();

            Path dir = Path.of("invoices");
            Files.createDirectories(dir);

            Path pdfPath = dir.resolve("invoice_" + orderId + ".pdf");

            PdfWriter writer = new PdfWriter(pdfPath.toString());
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("INVOICE")
                    .setBold()
                    .setFontSize(18));

            document.add(new Paragraph("Order ID: " + orderId));
            document.add(new Paragraph("Customer Email: " + email));
            document.add(new Paragraph("Total Price: $" + totalPrice));

            document.add(new Paragraph("\nItems:"));

            for (JsonNode item : payload.path("line_items")) {
                String title = item.path("title").asText();
                int qty = item.path("quantity").asInt();
                String price = item.path("price").asText();

                document.add(
                        new Paragraph("- " + title +
                                " | Qty: " + qty +
                                " | Price: $" + price)
                );
            }

            document.close();

            return pdfPath;

        } catch (IOException e) {
            throw new RuntimeException("PDF creation failed", e);
        }
    }
}
