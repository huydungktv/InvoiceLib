package com.huydungktv.invoice.service;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.validation.InvoiceValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DefaultInvoiceCalculator implements InvoiceCalculator {
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;
    private static final Logger LOGGER = Logger.getLogger(DefaultInvoiceCalculator.class.getName());

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

        for (int index = 0; index < items.size(); index++) {
            int itemIndex = index;
            InvoiceItemRequest item = items.get(index);
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

                LOGGER.info(() -> String.format(
                    "Calculated invoice item: index=%d, itemName=%s, quantity=%s, price=%s, vat=%s%%, priceVat=%s, subTotal=%s, subTotalVat=%s",
                    itemIndex,
                    item.itemName(),
                    item.quantity(),
                    money(item.price()),
                    item.vat(),
                    priceVat,
                    subTotal,
                    subTotalVat));
        }

            InvoiceResponse response = new InvoiceResponse(
                calculatedItems,
                money(total),
                money(totalPriceVat),
                money(totalVat));

            LOGGER.info(() -> String.format(
                "Calculated invoice totals: total=%s, totalPriceVat=%s, totalVat=%s",
                response.total(),
                response.totalPriceVat(),
                response.totalVat()));

            return response;
    }

    private BigDecimal money(BigDecimal value) {
        return value.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
