package com.huydungktv.invoice.service;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.validation.InvoiceValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class DefaultInvoiceCalculator implements InvoiceCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final InvoiceValidator validator;

    public DefaultInvoiceCalculator() {
        this(new InvoiceValidator());
    }

    public DefaultInvoiceCalculator(InvoiceValidator validator) {
        this.validator = validator;
    }

    @Override
    public InvoiceResponse calculate(List<InvoiceItemRequest> items) {
        validator.validate(items);

        List<InvoiceItemResponse> calculatedItems = new ArrayList<>(items.size());
        BigDecimal total = BigDecimal.ZERO;
        BigDecimal totalPriceVat = BigDecimal.ZERO;
        BigDecimal totalVat = BigDecimal.ZERO;

        for (InvoiceItemRequest item : items) {
            BigDecimal subTotal = money(item.quantity().multiply(item.price()));
            BigDecimal priceVat = money(subTotal.multiply(item.vat()).divide(ONE_HUNDRED, MONEY_SCALE, MONEY_ROUNDING));
            BigDecimal subTotalVat = money(subTotal.add(priceVat));

            calculatedItems.add(new InvoiceItemResponse(
                    item.itemName(),
                    item.quantity(),
                    money(item.price()),
                    item.vat(),
                    priceVat,
                    subTotal,
                    subTotalVat));

            total = total.add(subTotal);
            totalPriceVat = totalPriceVat.add(priceVat);
            totalVat = totalVat.add(subTotalVat);
        }

        return new InvoiceResponse(
                calculatedItems,
                money(total),
                money(totalPriceVat),
                money(totalVat));
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
