package com.huydungktv.invoice.service;

import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.exception.InvoiceValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DefaultInvoiceCalculatorTest {
    private final InvoiceCalculator calculator = new DefaultInvoiceCalculator();

    @Test
    void calculatesItemAndInvoiceTotals() {
        InvoiceResponse result = calculator.calculate(List.of(
                new InvoiceItemRequest("Laptop", decimal("2"), decimal("1000"), decimal("10")),
                new InvoiceItemRequest("Mouse", decimal("3"), decimal("20"), decimal("8"))
        ));

        InvoiceItemResponse laptop = result.items().get(0);
        assertEquals("Laptop", laptop.itemName());
        assertMoney("1000.00", laptop.price());
        assertMoney("10.00", laptop.vat());
        assertMoney("200.00", laptop.priceVat());
        assertMoney("2000.00", laptop.subTotal());
        assertMoney("2200.00", laptop.subTotalVat());

        InvoiceItemResponse mouse = result.items().get(1);
        assertMoney("4.80", mouse.priceVat());
        assertMoney("60.00", mouse.subTotal());
        assertMoney("64.80", mouse.subTotalVat());

        assertMoney("2060.00", result.total());
        assertMoney("204.80", result.totalPriceVat());
        assertMoney("2264.80", result.totalVat());
    }

    @Test
    void roundsMoneyToTwoDecimalPlaces() {
        InvoiceResponse result = calculator.calculate(List.of(
                new InvoiceItemRequest("Service", decimal("3"), decimal("0.99"), decimal("8.5"))
        ));

        assertMoney("2.97", result.total());
        assertMoney("0.25", result.totalPriceVat());
        assertMoney("3.22", result.totalVat());
    }

    @Test
    void rejectsEmptyInvoice() {
        assertThrows(InvoiceValidationException.class, () -> calculator.calculate(List.of()));
    }

    @Test
    void rejectsNegativePrice() {
        InvoiceItemRequest item = new InvoiceItemRequest(
                "Invalid", decimal("1"), decimal("-1"), decimal("10"));

        assertThrows(InvoiceValidationException.class, () -> calculator.calculate(List.of(item)));
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }

    private static void assertMoney(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual));
    }
}
