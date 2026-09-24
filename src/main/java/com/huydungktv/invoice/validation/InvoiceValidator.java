package com.huydungktv.invoice.validation;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.exception.InvoiceValidationException;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceValidator {
    /**
     * Validates all items in an invoice.
     *
     * @param items invoice items to validate
     * @throws InvoiceValidationException if the list or any item is invalid
     */
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

    /**
     * Validates the required fields of one invoice item.
     *
     * @param item invoice item to validate
     * @param index item position used in validation messages
     * @throws InvoiceValidationException if any item field is invalid
     */
    private void validateItem(InvoiceItemRequest item, int index) {
        String fieldPrefix = "Item at index " + index + ": ";
        if (item.itemName() == null || item.itemName().isBlank()) {
            throw new InvoiceValidationException(fieldPrefix + "itemName is required");
        }
        validateNonNegative(item.quantity(), fieldPrefix + "quantity", true);
        validateNonNegative(item.price(), fieldPrefix + "price", false);
        validateNonNegative(item.vat(), fieldPrefix + "vat", false);
    }

    /**
     * Validates that a numeric value is present and is not negative.
     *
     * @param value numeric value to validate
     * @param fieldName field name used in the validation message
     * @param mustBePositive whether the value must be greater than zero
     * @throws InvoiceValidationException if the value is null or invalid
     */
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
