# Integration Guide

## Mục tiêu tích hợp

Ứng dụng khách hàng nhúng InvoiceLib như một dependency và gọi trực tiếp service tính hóa đơn. InvoiceLib không chạy độc lập và không cung cấp REST controller.

```text
Ứng dụng khách hàng
	|
	v
Mapper của ứng dụng khách hàng
	|
	v
InvoiceItemRequest
	|
	v
InvoiceCalculator
	|
	v
InvoiceResponse
	|
	v
Mapper / persistence / API của ứng dụng khách hàng
```

## Phân tách model

Không nên truyền domain object của khách hàng trực tiếp vào logic nội bộ. Hãy map object đó sang `InvoiceItemRequest` ở boundary:

```java
InvoiceItemRequest request = new InvoiceItemRequest(
	customerItem.name(),
	customerItem.quantity(),
	customerItem.unitPriceBeforeVat(),
	customerItem.vatPercent()
);

InvoiceResponse response = calculator.calculate(List.of(request));
```

Sau khi tính xong, map `InvoiceResponse` sang model của khách hàng hoặc response API riêng. Cách này giúp InvoiceLib không phụ thuộc vào tên field, annotation hoặc framework của từng khách hàng.

## Xuất kết quả CSV

Để tạo nội dung CSV từ kết quả tính toán:

```java
InvoiceCsvMapper csvMapper = new InvoiceCsvMapper();
String csvContent = csvMapper.toCsv(response);
```

Có thể ghi nội dung ra file UTF-8 tại ứng dụng khách hàng:

```java
Files.writeString(
	Path.of("invoice.csv"),
	csvContent,
	StandardCharsets.UTF_8
);
```

CSV gồm các cột:

```text
rowType,itemName,quantity,price,vat,priceVat,subTotal,subTotalVat,total,totalPriceVat,totalVat
```

`ITEM` chứa dữ liệu từng item; `TOTAL` chứa `total`, `totalPriceVat` và `totalVat`. Mapper dùng CRLF làm line ending và tự escape giá trị CSV đặc biệt. Việc ghi file, upload hoặc trả response HTTP thuộc trách nhiệm của ứng dụng khách hàng.

## Tạo và phân phối PDF

Ứng dụng khách hàng có thể tạo PDF từ cùng `InvoiceResponse`:

```java
InvoicePdfResult pdfResult = new InvoicePdfMapper().toPdf(response);
String invoiceNumber = pdfResult.invoiceNumber();
byte[] pdfBytes = pdfResult.pdfBytes();
```

Lưu file:

```java
Files.write(Path.of("invoice-" + invoiceNumber + ".pdf"), pdfBytes);
```

Hoặc trả `pdfBytes` trong HTTP response với content type `application/pdf`. InvoiceLib không tự lưu file, upload hoặc mở endpoint.

Số hóa đơn được sinh theo `yyyyMMdd.HHmmss` bằng timezone mặc định của JVM. Vì độ chính xác chỉ đến giây, hệ thống có yêu cầu số duy nhất phải thêm cơ chế chống trùng ở application/database layer.

PDF hiện dùng font chuẩn PDF; tên item có ký tự ngoài ASCII sẽ được thay bằng `?`. Nếu khách hàng cần tiếng Việt đầy đủ, cần một font Unicode được cấp phép và một phiên bản PDF mapper hỗ trợ nhúng font.

## Tích hợp Maven

```xml
<dependency>
    <groupId>com.huydungktv</groupId>
    <artifactId>invoicelib</artifactId>
    <version>0.0.1</version>
</dependency>
```

Đảm bảo runtime của ứng dụng dùng Java 21 hoặc tương thích với bytecode Java 21.

## Quản lý instance

`DefaultInvoiceCalculator` không giữ state của hóa đơn giữa các lần gọi. Có thể tạo một instance dùng chung trong ứng dụng:

```java
@ApplicationScoped
public class InvoiceService {
    private final InvoiceCalculator calculator = new DefaultInvoiceCalculator();
}
```

Annotation trong ví dụ thuộc framework của ứng dụng khách hàng, không thuộc InvoiceLib. Nếu framework có dependency injection, đăng ký `InvoiceCalculator` bằng cấu hình của framework đó.

## Lưu kết quả

InvoiceLib chỉ trả về kết quả tính toán và không lưu dữ liệu. Ứng dụng khách hàng nên:

- Lưu input gốc nếu cần audit.
- Lưu output đã tính cùng version thư viện.
- Quyết định định dạng tiền tệ và currency code ở domain của mình.
- Không dùng số tiền đã format thành String để thực hiện phép tính tiếp theo.
- Dùng `InvoiceCsvMapper` chỉ sau khi đã hoàn tất việc tính toán và kiểm tra `InvoiceResponse`.

## Tích hợp API HTTP

Nếu ứng dụng có REST API, controller nên nhận request model riêng rồi chuyển đổi sang InvoiceLib:

```java
public InvoiceHttpResponse calculate(InvoiceHttpRequest request) {
    List<InvoiceItemRequest> items = request.items().stream()
	    .map(item -> new InvoiceItemRequest(
		    item.name(),
		    item.quantity(),
		    item.price(),
		    item.vat()))
	    .toList();

    InvoiceResponse response = calculator.calculate(items);
    return InvoiceHttpResponse.from(response);
}
```

Không đưa web dependency vào InvoiceLib core chỉ để phục vụ controller của một ứng dụng.

## Versioning và bàn giao

- Pin version cụ thể trong ứng dụng khách hàng, không dùng version động.
- Đọc `CHANGELOG.md` trước khi nâng version.
- Chạy test của ứng dụng khách hàng sau khi nâng version.
- Kiểm tra các giá trị biên: VAT 0%, VAT phân số, giá trị tiền có nhiều chữ số và nhiều item.
- Lưu lại JAR, checksum và version trong artifact repository nếu bàn giao offline.

## Các tích hợp chưa có

Các chức năng sau chưa thuộc core hiện tại:

- Lưu database.
- Sinh số hóa đơn.
- Xuất PDF, XML hoặc format thuế.
- Kết nối hệ thống thuế.
- Gửi email hoặc phát hành hóa đơn điện tử.

Nên triển khai các chức năng này trong adapter/module của ứng dụng khách hàng hoặc module riêng, để giữ InvoiceLib nhỏ và dễ tái sử dụng.
