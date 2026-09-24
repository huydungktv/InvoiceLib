# InvoiceLib

Thư viện Java dùng để tính tiền hóa đơn theo từng sản phẩm hoặc dịch vụ, bao gồm tiền trước VAT, tiền VAT và tiền sau VAT.

InvoiceLib là thư viện core dạng JAR:

- Không có giao diện web.
- Không có database.
- Không phụ thuộc Spring Boot.
- Nhận input bằng các class typed.
- Trả về output bằng các class typed.
- Dùng `BigDecimal` cho các phép tính tiền tệ.

## Yêu cầu môi trường

- Java 21 trở lên.
- Maven 3.9 trở lên nếu build từ source.

## Cài đặt

Build từ source:

```bash
mvn clean package
```

File JAR được tạo tại `target/invoicelib-0.0.1.jar`.

Cài vào Maven local repository:

```bash
mvn clean install
```

Sau đó thêm dependency vào project khách hàng:

```xml
<dependency>
    <groupId>com.huydungktv</groupId>
    <artifactId>invoicelib</artifactId>
    <version>0.0.1</version>
</dependency>
```

Hiện tại thư viện chưa được publish lên Maven Central hoặc repository riêng.

## Cách sử dụng nhanh

```java
import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.service.InvoiceCalculator;
import com.huydungktv.invoice.service.DefaultInvoiceCalculator;

import java.math.BigDecimal;
import java.util.List;

InvoiceCalculator calculator = new DefaultInvoiceCalculator();

InvoiceResponse invoice = calculator.calculate(List.of(
	new InvoiceItemRequest(
		"Laptop",
		new BigDecimal("2"),
		new BigDecimal("1000"),
		new BigDecimal("10")
	),
	new InvoiceItemRequest(
		"Mouse",
		new BigDecimal("3"),
		new BigDecimal("20"),
		new BigDecimal("8")
	)
));

System.out.println(invoice.total());
System.out.println(invoice.totalPriceVat());
System.out.println(invoice.totalVat());
```

## Input

Method chính:

```java
InvoiceResponse calculate(List<InvoiceItemRequest> items)
```

Mỗi item được tạo bằng `InvoiceItemRequest`:

| Field | Kiểu | Bắt buộc | Ý nghĩa |
|---|---|---:|---|
| `itemName` | `String` | Có | Tên sản phẩm hoặc dịch vụ |
| `quantity` | `BigDecimal` | Có | Số lượng, phải lớn hơn `0` |
| `price` | `BigDecimal` | Có | Đơn giá trước VAT, không được âm |
| `vat` | `BigDecimal` | Có | Phần trăm VAT, ví dụ `10` là 10% |

Ví dụ một item:

```java
InvoiceItemRequest item = new InvoiceItemRequest(
	"Laptop",
	new BigDecimal("2"),
	new BigDecimal("1000"),
	new BigDecimal("10")
);
```

Nên truyền giá trị tiền và số lượng bằng chuỗi khi tạo `BigDecimal`:

```java
new BigDecimal("1000.50")
```

Không nên dùng `new BigDecimal(1000.50)` vì `double` có thể chứa sai số nhị phân.

## Output

### Output từng item

Mỗi item trong `InvoiceResponse.items()` là `InvoiceItemResponse`:

| Field | Ý nghĩa |
|---|---|
| `itemName` | Tên sản phẩm hoặc dịch vụ |
| `quantity` | Số lượng |
| `price` | Đơn giá trước VAT |
| `vat` | Phần trăm VAT |
| `priceVat` | Tiền VAT của item |
| `subTotal` | Tiền trước VAT của item |
| `subTotalVat` | Tiền sau VAT của item |

### Output tổng hóa đơn

| Field | Ý nghĩa |
|---|---|
| `items` | Danh sách kết quả theo từng item |
| `total` | Tổng tiền trước VAT của hóa đơn |
| `totalPriceVat` | Tổng tiền VAT của hóa đơn |
| `totalVat` | Tổng tiền sau VAT của hóa đơn |

Lưu ý: `totalPriceVat` là tiền VAT, còn `totalVat` là tổng tiền đã bao gồm VAT.

## Công thức tính

Với mỗi item:

```text
subTotal    = quantity x price
priceVat    = subTotal x vat / 100
subTotalVat = subTotal + priceVat
```

Tổng hóa đơn:

```text
total         = tổng subTotal của tất cả item
totalPriceVat = tổng priceVat của tất cả item
totalVat      = tổng subTotalVat của tất cả item
```

Với hai item trong ví dụ trên:

```text
items[0].subTotal    = 2000.00
items[0].priceVat    = 200.00
items[0].subTotalVat = 2200.00

items[1].subTotal    = 60.00
items[1].priceVat    = 4.80
items[1].subTotalVat = 64.80

total         = 2060.00
totalPriceVat = 204.80
totalVat      = 2264.80
```

## Quy tắc tiền tệ và làm tròn

- Các giá trị tiền sử dụng `BigDecimal`.
- Các giá trị tiền được làm tròn đến 2 chữ số thập phân.
- Phương thức làm tròn là `RoundingMode.HALF_UP`.
- VAT là phần trăm, không phải hệ số. Giá trị `8.5` nghĩa là 8.5%.
- VAT được tính trên `subTotal` đã làm tròn đến 2 chữ số.
- Tổng hóa đơn được cộng từ kết quả đã tính theo từng item.

## Validation và lỗi

Khi input không hợp lệ, thư viện ném `InvoiceValidationException`.

Các điều kiện kiểm tra:

- Danh sách item không được `null` hoặc rỗng.
- Item không được `null`.
- `itemName` không được `null` hoặc blank.
- `quantity` không được `null` và phải lớn hơn `0`.
- `price` không được `null` và không được âm.
- `vat` không được `null` và không được âm.

Ví dụ xử lý lỗi:

```java
try {
	InvoiceResponse invoice = calculator.calculate(items);
} catch (InvoiceValidationException exception) {
	System.err.println(exception.getMessage());
}
```

## API public

Các class chính dành cho khách hàng sử dụng:

```text
com.huydungktv.invoice.api.InvoiceItemRequest
com.huydungktv.invoice.api.InvoiceItemResponse
com.huydungktv.invoice.api.InvoiceResponse
com.huydungktv.invoice.service.InvoiceCalculator
com.huydungktv.invoice.service.DefaultInvoiceCalculator
com.huydungktv.invoice.exception.InvoiceValidationException
com.huydungktv.invoice.mapper.InvoiceCsvMapper
com.huydungktv.invoice.mapper.InvoicePdfMapper
com.huydungktv.invoice.api.InvoicePdfResult
```

Khuyến nghị sử dụng interface `InvoiceCalculator` trong code ứng dụng và chỉ khởi tạo `DefaultInvoiceCalculator` tại composition root hoặc cấu hình dependency injection của ứng dụng khách hàng.

## Chuyển output hóa đơn thành CSV

`InvoiceCsvMapper` chuyển một `InvoiceResponse` thành nội dung CSV dạng `String`:

```java
InvoiceCsvMapper csvMapper = new InvoiceCsvMapper();
String csvContent = csvMapper.toCsv(invoice);
```

Thư viện không tự ghi file. Ứng dụng khách hàng có thể tự lưu nội dung này:

```java
Files.writeString(Path.of("invoice.csv"), csvContent, StandardCharsets.UTF_8);
```

CSV gồm dòng header, các dòng `ITEM` và một dòng `TOTAL`:

```text
rowType,itemName,quantity,price,vat,priceVat,subTotal,subTotalVat,total,totalPriceVat,totalVat
ITEM,Laptop,2,1000.00,10,200.00,2000.00,2200.00,,,
TOTAL,,,,,,,,2000.00,200.00,2200.00
```

Giá trị `itemName` có dấu phẩy, dấu nháy kép hoặc xuống dòng sẽ được escape theo chuẩn CSV.

## Tạo PDF hóa đơn

`InvoicePdfMapper` chuyển `InvoiceResponse` thành kết quả PDF gồm số hóa đơn và nội dung PDF dạng `byte[]`:

```java
InvoicePdfMapper pdfMapper = new InvoicePdfMapper();
InvoicePdfResult pdfResult = pdfMapper.toPdf(invoice);

String invoiceNumber = pdfResult.invoiceNumber();
byte[] pdfBytes = pdfResult.pdfBytes();
```

Ghi PDF ra file ở ứng dụng khách hàng:

```java
Files.write(
	Path.of("invoice-" + invoiceNumber + ".pdf"),
	pdfBytes
);
```

Số hóa đơn được sinh tại thời điểm gọi API theo timezone hệ thống, format `yyyyMMdd.HHmmss`.

Ví dụ:

```text
20260924.135145
```

PDF chứa tiêu đề, số hóa đơn, bảng chi tiết item và ba tổng tiền: trước VAT, tiền VAT và sau VAT. API không tự ghi file hoặc gửi PDF qua HTTP.

Lưu ý: bản PDF hiện dùng font chuẩn PDF với tập ký tự ASCII. Ký tự ngoài ASCII trong `itemName` sẽ được thay bằng `?`; nếu cần hiển thị đầy đủ tiếng Việt có dấu, cần bổ sung font Unicode nhúng vào PDF ở phiên bản tiếp theo.

## Cấu trúc project

```text
InvoiceLib/
├── pom.xml
├── README.md
├── CHANGELOG.md
├── docs/
└── src/
	├── main/java/com/huydungktv/invoice/
	│   ├── api/        # Input và output public
	│   ├── service/    # Logic tính hóa đơn
	│   ├── validation/ # Kiểm tra input
	│   ├── mapper/     # Dành cho chuyển đổi object khi tích hợp
	│   ├── exception/  # Exception của thư viện
	│   └── spi/        # Extension point cho tích hợp mở rộng
	└── test/java/com/huydungktv/invoice/
		├── api/
		├── service/
		├── validation/
		└── contract/
```

## Kiểm thử

Chạy toàn bộ test:

```bash
mvn clean test
```

Đóng gói JAR:

```bash
mvn clean package
```

## Giới hạn hiện tại

- Chưa lưu hóa đơn vào database.
- Chưa sinh mã hóa đơn.
- Chưa xuất PDF hoặc XML.
- Chưa kết nối hệ thống thuế hoặc dịch vụ bên ngoài.
- Chưa có API HTTP.
- Chưa có cơ chế cấu hình riêng theo từng khách hàng.

Các chức năng trên có thể được bổ sung ở lớp adapter hoặc module tích hợp mà không đưa dependency web vào core library.