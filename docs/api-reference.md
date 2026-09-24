# API Reference

Tài liệu này mô tả API public của InvoiceLib. Các class trong package `api`, interface service và exception public là contract dành cho ứng dụng khách hàng.

## Entry point

```java
public interface InvoiceCalculator {
    InvoiceResponse calculate(List<InvoiceItemRequest> items);
}
```

Implementation mặc định:

```java
InvoiceCalculator calculator = new DefaultInvoiceCalculator();
InvoiceResponse response = calculator.calculate(items);
```

Ứng dụng nên phụ thuộc vào `InvoiceCalculator`, không phụ thuộc trực tiếp vào chi tiết implementation nếu không cần thiết.

## InvoiceItemRequest

```java
public record InvoiceItemRequest(
	String itemName,
	BigDecimal quantity,
	BigDecimal price,
	BigDecimal vat
) {}
```

| Field | Kiểu | Quy tắc |
|---|---|---|
| `itemName` | `String` | Bắt buộc, không được null hoặc blank |
| `quantity` | `BigDecimal` | Bắt buộc, phải lớn hơn 0 |
| `price` | `BigDecimal` | Bắt buộc, không được âm |
| `vat` | `BigDecimal` | Bắt buộc, không được âm; `10` nghĩa là 10% |

`price` là đơn giá trước VAT, không phải giá đã bao gồm VAT.

## InvoiceItemResponse

```java
public record InvoiceItemResponse(
	String itemName,
	BigDecimal quantity,
	BigDecimal price,
	BigDecimal vat,
	BigDecimal priceVat,
	BigDecimal subTotal,
	BigDecimal subTotalVat
) {}
```

| Field | Ý nghĩa |
|---|---|
| `itemName` | Tên item được giữ lại từ input |
| `quantity` | Số lượng được giữ lại từ input |
| `price` | Đơn giá trước VAT, làm tròn 2 chữ số |
| `vat` | Phần trăm VAT |
| `priceVat` | Tiền VAT của item |
| `subTotal` | `quantity x price` trước VAT |
| `subTotalVat` | `subTotal + priceVat` |

## InvoiceResponse

```java
public record InvoiceResponse(
	List<InvoiceItemResponse> items,
	BigDecimal total,
	BigDecimal totalPriceVat,
	BigDecimal totalVat
) {}
```

| Field | Ý nghĩa |
|---|---|
| `items` | Danh sách output theo đúng thứ tự input |
| `total` | Tổng tiền trước VAT |
| `totalPriceVat` | Tổng tiền VAT |
| `totalVat` | Tổng tiền sau VAT |

Danh sách `items` được copy bất biến khi tạo `InvoiceResponse`.

## Công thức

Với mỗi item:

```text
subTotal    = quantity x price
priceVat    = subTotal x vat / 100
subTotalVat = subTotal + priceVat
```

Ở cấp hóa đơn:

```text
total         = sum(subTotal)
totalPriceVat = sum(priceVat)
totalVat      = sum(subTotalVat)
```

Các giá trị tiền được làm tròn 2 chữ số bằng `RoundingMode.HALF_UP`. VAT được tính trên `subTotal` sau khi đã làm tròn.

## Public API ổn định

Các type hiện được thiết kế để ứng dụng khách hàng sử dụng:

```text
com.huydungktv.invoice.api.InvoiceItemRequest
com.huydungktv.invoice.api.InvoiceItemResponse
com.huydungktv.invoice.api.InvoiceResponse
com.huydungktv.invoice.service.InvoiceCalculator
com.huydungktv.invoice.service.DefaultInvoiceCalculator
com.huydungktv.invoice.exception.InvoiceValidationException
```

Các class trong `validation`, `mapper` và `spi` là điểm mở rộng nội bộ hoặc dành cho các phiên bản tích hợp sau; không nên tự tạo dependency vào class chưa được ghi trong danh sách public API.
