package com.huydungktv.invoice.api;

import java.math.BigDecimal;
import java.util.List;

public record InvoiceResponse(
        List<InvoiceItemResponse> items,
        BigDecimal total,
        BigDecimal totalPriceVat,
        BigDecimal totalVat
) {
    public InvoiceResponse {
        items = List.copyOf(items);
    }
}
