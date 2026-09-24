package com.huydungktv.invoice.contract;

import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoicePdfResult;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.mapper.InvoicePdfMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InvoicePdfMapperTest {
    private static final Path PDF_OUTPUT_DIRECTORY = Path.of(
            "D:\\InvoiceLib\\src\\test\\java\\com\\huydungktv\\invoice\\invoicePdf");
    private final InvoicePdfMapper mapper = new InvoicePdfMapper();

    @Test
    void createsPdfWithGeneratedInvoiceNumber() throws Exception {
        InvoiceResponse invoice = new InvoiceResponse(
                List.of(new InvoiceItemResponse(
                        "Laptop",
                        decimal("2"),
                        decimal("1000.00"),
                        decimal("10"),
                        decimal("200.00"),
                        decimal("2000.00"),
                        decimal("2200.00"))),
                decimal("2000.00"),
                decimal("200.00"),
                decimal("2200.00"));

        InvoicePdfResult result = mapper.toPdf(invoice);
        Files.createDirectories(PDF_OUTPUT_DIRECTORY);
        Path pdfPath = PDF_OUTPUT_DIRECTORY.resolve("invoice-" + result.invoiceNumber() + ".pdf");
        Files.write(pdfPath, result.pdfBytes());

        assertTrue(result.invoiceNumber().matches("\\d{8}\\.\\d{6}"));
        assertNotNull(result.pdfBytes());
        assertTrue(result.pdfBytes().length > 0);
        assertTrue(Files.exists(pdfPath));
        assertTrue(Files.size(pdfPath) > 0);
        assertEquals('%', (char) result.pdfBytes()[0]);
        assertEquals('P', (char) result.pdfBytes()[1]);
        assertEquals('D', (char) result.pdfBytes()[2]);
        assertEquals('F', (char) result.pdfBytes()[3]);

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(result.pdfBytes()))) {
            assertEquals(1, document.getNumberOfPages());
            String text = new PDFTextStripper().getText(document);
            assertTrue(text.contains(result.invoiceNumber()));
            assertTrue(text.contains("2000.00"));
            assertTrue(text.contains("2200.00"));
        }
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
