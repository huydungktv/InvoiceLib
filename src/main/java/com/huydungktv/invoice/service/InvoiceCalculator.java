package com.huydungktv.invoice.service;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceResponse;

import java.util.List;

public interface InvoiceCalculator {
    /**
     * Calculates the amounts of every invoice item and the invoice totals.
     *
     * <p>For each item, the calculation is:</p>
     * <ul>
     *     <li>{@code subTotal = quantity x price}</li>
     *     <li>{@code priceVat = subTotal x vat / 100}</li>
     *     <li>{@code subTotalVat = subTotal + priceVat}</li>
     * </ul>
     *
     * <p>Money values are rounded to two decimal places using
     * {@code RoundingMode.HALF_UP}. The {@code vat} value is a percentage;
     * for example, {@code 10} represents 10% VAT.</p>
     *
     * @param items invoice items to calculate; the list must contain at least
     *              one non-null item
     * @return calculated item values and invoice totals
     * @throws com.huydungktv.invoice.exception.InvoiceValidationException
     *         when the list or any item contains invalid data
     */
    InvoiceResponse calculate(List<InvoiceItemRequest> items);
}
