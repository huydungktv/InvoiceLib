package com.huydungktv.invoice.contract;

import com.huydungktv.invoice.api.InvoiceItemResponse;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.mapper.InvoiceCsvMapper;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InvoiceCsvMapperTest {
    private final InvoiceCsvMapper mapper = new InvoiceCsvMapper();

    @Test
    void mapsItemsAndTotalsToCsv() {
        InvoiceResponse invoice = new InvoiceResponse(
                List.of(new InvoiceItemResponse(
                        "Laptop",
                        decimal("2"),
                        decimal("1000.00"),
                        decimal("10"),
                        decimal("200.00"),
                        decimal("2000.00"),
                        decimal("2200.00"))),
                decimal("2000.00"),
                decimal("200.00"),
                decimal("2200.00"));

        String expected = "rowType,itemName,quantity,price,vat,priceVat,subTotal,subTotalVat,total,totalPriceVat,totalVat\r\n"
                + "ITEM,Laptop,2,1000.00,10,200.00,2000.00,2200.00,,,\r\n"
                + "TOTAL,,,,,,,,2000.00,200.00,2200.00\r\n";

        assertEquals(expected, mapper.toCsv(invoice));
    }

    @Test
    void escapesCsvSpecialCharacters() {
        InvoiceResponse invoice = new InvoiceResponse(
                List.of(new InvoiceItemResponse(
                        "Cable, \"premium\"\nblack",
                        decimal("1"),
                        decimal("10"),
                        decimal("0"),
                        decimal("0"),
                        decimal("10"),
                        decimal("10"))),
                decimal("10"),
                decimal("0"),
                decimal("10"));

        String csv = mapper.toCsv(invoice);

        assertEquals(true, csv.contains("\"Cable, \"\"premium\"\"\nblack\""));
    }

    @Test
    void rejectsNullInvoice() {
        assertThrows(NullPointerException.class, () -> mapper.toCsv(null));
    }

    private static BigDecimal decimal(String value) {
        return new BigDecimal(value);
    }
}
