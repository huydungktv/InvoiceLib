package com.huydungktv.invoice.validation;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.exception.InvoiceValidationException;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceValidator {
    public void validate(List<InvoiceItemRequest> items) {
        if (items == null || items.isEmpty()) {
            throw new InvoiceValidationException("Invoice must contain at least one item");
        }

        for (int index = 0; index < items.size(); index++) {
            InvoiceItemRequest item = items.get(index);
            if (item == null) {
                throw new InvoiceValidationException("Item at index " + index + " must not be null");
            }
            validateItem(item, index);
        }
    }

    private void validateItem(InvoiceItemRequest item, int index) {
        String fieldPrefix = "Item at index " + index + ": ";
        if (item.itemName() == null || item.itemName().isBlank()) {
            throw new InvoiceValidationException(fieldPrefix + "itemName is required");
        }
        validateNonNegative(item.quantity(), fieldPrefix + "quantity", true);
        validateNonNegative(item.price(), fieldPrefix + "price", false);
        validateNonNegative(item.vat(), fieldPrefix + "vat", false);
    }

    private void validateNonNegative(BigDecimal value, String fieldName, boolean mustBePositive) {
        if (value == null) {
            throw new InvoiceValidationException(fieldName + " is required");
        }
        int comparison = value.compareTo(BigDecimal.ZERO);
        if (mustBePositive ? comparison <= 0 : comparison < 0) {
            throw new InvoiceValidationException(fieldName + (mustBePositive
                    ? " must be greater than zero"
                    : " must not be negative"));
        }
    }
}
