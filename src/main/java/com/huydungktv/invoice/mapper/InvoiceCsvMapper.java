package com.huydungktv.invoice.mapper;

import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoiceResponse;

import java.util.Objects;
import java.util.StringJoiner;
import java.util.logging.Logger;

public class InvoiceCsvMapper {
    private static final String LINE_SEPARATOR = "\r\n";
    private static final String HEADER = "rowType,itemName,quantity,price,vat,priceVat,subTotal,subTotalVat,total,totalPriceVat,totalVat";
    private static final Logger LOGGER = Logger.getLogger(InvoiceCsvMapper.class.getName());

    public String toCsv(InvoiceResponse invoice) {
        Objects.requireNonNull(invoice, "invoice must not be null");

        LOGGER.info(() -> "Starting invoice CSV conversion: itemCount=" + invoice.items().size());

        StringBuilder csv = new StringBuilder(HEADER).append(LINE_SEPARATOR);
        for (int index = 0; index < invoice.items().size(); index++) {
            int itemIndex = index;
            InvoiceItemResponse item = invoice.items().get(index);
            csv.append(row(
                    "ITEM",
                    item.itemName(),
                    item.quantity(),
                    item.price(),
                    item.vat(),
                    item.priceVat(),
                    item.subTotal(),
                    item.subTotalVat(),
                    null,
                    null,
                    null));
        }

        csv.append(row(
                "TOTAL",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                invoice.total(),
                invoice.totalPriceVat(),
                invoice.totalVat()));

            LOGGER.info(() -> String.format(
                "Completed invoice CSV conversion: total=%s, totalPriceVat=%s, totalVat=%s",
                invoice.total(),
                invoice.totalPriceVat(),
                invoice.totalVat()));

        return csv.toString();
    }

    private String row(Object... values) {
        StringJoiner row = new StringJoiner(",");
        for (Object value : values) {
            row.add(escape(value == null ? "" : value.toString()));
        }
        return row + LINE_SEPARATOR;
    }

    private String escape(String value) {
        if (value.indexOf(',') >= 0 || value.indexOf('"') >= 0
                || value.indexOf('\r') >= 0 || value.indexOf('\n') >= 0) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }
}
