# Error Handling

## Nguyên tắc

InvoiceLib báo lỗi input bằng exception typed thay vì trả về `null` hoặc một object kết quả không hoàn chỉnh. Ứng dụng khách hàng chịu trách nhiệm chuyển exception thành thông báo, HTTP response hoặc mã lỗi theo hệ thống của mình.

## Exception hiện tại

```java
com.huydungktv.invoice.exception.InvoiceValidationException
```

Đây là `RuntimeException`, được ném khi danh sách item hoặc một item vi phạm validation.

Ví dụ:

```java
try {
	InvoiceResponse response = calculator.calculate(items);
} catch (InvoiceValidationException exception) {
	System.err.println(exception.getMessage());
}
```

## Các lỗi validation

| Điều kiện | Message hiện tại |
|---|---|
| Danh sách null hoặc rỗng | `Invoice must contain at least one item` |
| Item null | `Item at index {index} must not be null` |
| Tên item null hoặc blank | `Item at index {index}: itemName is required` |
| Quantity null | `Item at index {index}: quantity is required` |
| Quantity không dương | `Item at index {index}: quantity must be greater than zero` |
| Price null | `Item at index {index}: price is required` |
| Price âm | `Item at index {index}: price must not be negative` |
| VAT null | `Item at index {index}: vat is required` |
| VAT âm | `Item at index {index}: vat must not be negative` |

`{index}` bắt đầu từ `0`, theo vị trí item trong danh sách input.

## Khuyến nghị xử lý

1. Validate request ở boundary của ứng dụng nếu có thể, để trả lỗi sớm cho người dùng.
2. Vẫn giữ `try/catch` khi gọi thư viện vì validation của thư viện là lớp bảo vệ cuối cùng.
3. Không hiển thị stack trace hoặc thông tin nội bộ cho người dùng cuối.
4. Ghi log message và correlation id tại ứng dụng khách hàng nếu có hệ thống tracing.
5. Không retry lỗi validation; cần sửa input trước khi gọi lại.

Ví dụ khi tích hợp HTTP:

```java
try {
	return calculator.calculate(request.items());
} catch (InvoiceValidationException exception) {
	throw new BadRequestException(exception.getMessage(), exception);
}
```

`BadRequestException` trong ví dụ là exception của ứng dụng khách hàng, không phải class của InvoiceLib.

## Lỗi runtime trong tương lai

Các lỗi từ database, API thuế hoặc hệ thống ngoài chưa thuộc phạm vi core hiện tại. Khi bổ sung integration adapter, nên tách riêng các nhóm lỗi như timeout, unavailable và invalid response; không nên dùng `InvoiceValidationException` cho lỗi hạ tầng.
