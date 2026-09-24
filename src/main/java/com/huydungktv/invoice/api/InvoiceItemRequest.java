package com.huydungktv.invoice.api;

import java.math.BigDecimal;

public record InvoiceItemRequest(
        String itemName,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal vat
) {
}
