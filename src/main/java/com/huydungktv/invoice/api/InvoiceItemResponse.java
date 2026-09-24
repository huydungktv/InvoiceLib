package com.huydungktv.invoice.api;

import java.math.BigDecimal;

public record InvoiceItemResponse(
        String itemName,
        BigDecimal quantity,
        BigDecimal price,
        BigDecimal vat,
        BigDecimal priceVat,
        BigDecimal subTotal,
        BigDecimal subTotalVat
) {
}
