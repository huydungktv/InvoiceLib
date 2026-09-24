# Configuration

## Trạng thái hiện tại

InvoiceLib hiện không có cấu hình runtime. Đây là thư viện core thuần Java và phép tính được xác định bởi input truyền vào method:

```java
InvoiceResponse calculate(List<InvoiceItemRequest> items)
```

Không cần:

- File `application.properties`.
- Biến môi trường.
- Port HTTP.
- Kết nối database.
- Spring Boot configuration.

## Các quy tắc cố định

| Quy tắc | Giá trị hiện tại |
|---|---|
| Tiền tệ | `BigDecimal` |
| Số chữ số tiền | 2 |
| Cách làm tròn | `RoundingMode.HALF_UP` |
| Đơn vị VAT | Phần trăm, ví dụ `10` = 10% |
| Số item tối thiểu | 1 |
| Quantity | Lớn hơn 0 |
| Price | Lớn hơn hoặc bằng 0 |
| VAT | Lớn hơn hoặc bằng 0 |

Các quy tắc này không được thay đổi bằng cấu hình bên ngoài trong version hiện tại.

## Khởi tạo service

Khởi tạo mặc định:

```java
InvoiceCalculator calculator = new DefaultInvoiceCalculator();
```

`DefaultInvoiceCalculator` cũng nhận một `InvoiceValidator` khi cần thay thế validator:

```java
InvoiceValidator validator = new InvoiceValidator();
InvoiceCalculator calculator = new DefaultInvoiceCalculator(validator);
```

Ứng dụng có thể đăng ký `InvoiceCalculator` vào dependency injection framework của riêng mình. InvoiceLib không tự cung cấp container hoặc auto-configuration.

## Định hướng cấu hình tương lai

Nếu nhu cầu nghiệp vụ yêu cầu cấu hình, nên bổ sung thông qua một object options rõ ràng thay vì biến static hoặc đọc trực tiếp biến môi trường. Các khả năng có thể cân nhắc:

- Số chữ số làm tròn.
- Rounding mode.
- Chính sách VAT.
- Chính sách cho phép quantity bằng 0.

Mọi thay đổi làm ảnh hưởng kết quả tính toán phải đi kèm versioning, test hồi quy và tài liệu migration. Không nên âm thầm thay đổi quy tắc trong một minor version.
