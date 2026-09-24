package com.huydungktv.invoice.service;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceResponse;

import java.util.List;

public interface InvoiceCalculator {
    InvoiceResponse calculate(List<InvoiceItemRequest> items);
}
