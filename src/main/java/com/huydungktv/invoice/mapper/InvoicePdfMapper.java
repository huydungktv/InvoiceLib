package com.huydungktv.invoice.mapper;

import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoicePdfResult;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.exception.InvoicePdfException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.logging.Logger;

public class InvoicePdfMapper {
    private static final Logger LOGGER = Logger.getLogger(InvoicePdfMapper.class.getName());
    private static final DateTimeFormatter INVOICE_NUMBER_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd.HHmmss");
    private static final float MARGIN = 40;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float ROW_HEIGHT = 18;

    public InvoicePdfResult toPdf(InvoiceResponse invoice) {
        Objects.requireNonNull(invoice, "invoice must not be null");

        String invoiceNumber = LocalDateTime.now().format(INVOICE_NUMBER_FORMAT);
        LOGGER.info(() -> "Starting invoice PDF conversion: invoiceNumber=" + invoiceNumber
                + ", itemCount=" + invoice.items().size());

        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                drawHeader(content, invoiceNumber);
                drawItems(content, invoice);
                drawTotals(content, invoice);
            }

            document.save(output);
            InvoicePdfResult result = new InvoicePdfResult(invoiceNumber, output.toByteArray());
            LOGGER.info(() -> "Completed invoice PDF conversion: invoiceNumber=" + invoiceNumber
                    + ", total=" + invoice.total()
                    + ", totalPriceVat=" + invoice.totalPriceVat()
                    + ", totalVat=" + invoice.totalVat());
            return result;
        } catch (IOException | RuntimeException exception) {
            if (exception instanceof InvoicePdfException invoicePdfException) {
                throw invoicePdfException;
            }
            throw new InvoicePdfException("Could not create invoice PDF", exception);
        }
    }

    private void drawHeader(PDPageContentStream content, String invoiceNumber) throws IOException {
        write(content, "INVOICE", MARGIN, PAGE_HEIGHT - 55, 18, PDType1Font.HELVETICA_BOLD);
        write(content, "Invoice number: " + invoiceNumber,
                MARGIN, PAGE_HEIGHT - 78, 10, PDType1Font.HELVETICA);
    }

    private void drawItems(PDPageContentStream content, InvoiceResponse invoice) throws IOException {
        float y = PAGE_HEIGHT - 115;
        String[] headers = {"Item", "Qty", "Price", "VAT %", "VAT amount", "Before VAT", "After VAT"};
        float[] widths = {150, 42, 70, 48, 72, 72, 72};

        drawRow(content, headers, widths, MARGIN, y, true);
        y -= ROW_HEIGHT;

        for (InvoiceItemResponse item : invoice.items()) {
            String[] values = {
                    safeText(item.itemName()),
                    item.quantity().toPlainString(),
                    item.price().toPlainString(),
                    item.vat().toPlainString(),
                    item.priceVat().toPlainString(),
                    item.subTotal().toPlainString(),
                    item.subTotalVat().toPlainString()
            };
            drawRow(content, values, widths, MARGIN, y, false);
            y -= ROW_HEIGHT;
        }
    }

    private void drawTotals(PDPageContentStream content, InvoiceResponse invoice) throws IOException {
        float x = PAGE_WIDTH - MARGIN - 220;
        float y = 145;
        write(content, "Total before VAT: " + invoice.total(), x, y, 11, PDType1Font.HELVETICA_BOLD);
        write(content, "Total VAT: " + invoice.totalPriceVat(), x, y - 20, 11, PDType1Font.HELVETICA_BOLD);
        write(content, "Total after VAT: " + invoice.totalVat(), x, y - 40, 11, PDType1Font.HELVETICA_BOLD);
    }

    private void drawRow(PDPageContentStream content, String[] values, float[] widths,
                         float x, float y, boolean header) throws IOException {
        float currentX = x;
        for (int index = 0; index < values.length; index++) {
            write(content, values[index], currentX, y, 8, header
                    ? PDType1Font.HELVETICA_BOLD
                    : PDType1Font.HELVETICA);
            currentX += widths[index];
        }
    }

    private void write(PDPageContentStream content, String text, float x, float y,
                       float fontSize, PDType1Font font) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);
        content.showText(safeText(text));
        content.endText();
    }

    private String safeText(String text) {
        if (text == null) {
            return "";
        }
        return text.chars()
                .mapToObj(character -> character >= 32 && character <= 126
                        ? String.valueOf((char) character)
                        : "?")
                .reduce("", String::concat);
    }
}
