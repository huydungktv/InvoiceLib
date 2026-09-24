package com.huydungktv.invoice.api;

import java.util.Arrays;
import java.util.Objects;

public record InvoicePdfResult(
        String invoiceNumber,
        byte[] pdfBytes
) {
    public InvoicePdfResult {
        Objects.requireNonNull(invoiceNumber, "invoiceNumber must not be null");
        Objects.requireNonNull(pdfBytes, "pdfBytes must not be null");
        pdfBytes = Arrays.copyOf(pdfBytes, pdfBytes.length);
    }

    @Override
    public byte[] pdfBytes() {
        return Arrays.copyOf(pdfBytes, pdfBytes.length);
    }
}
