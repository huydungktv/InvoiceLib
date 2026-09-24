# Getting Started

Hướng dẫn này giúp một ứng dụng Java tích hợp InvoiceLib và thực hiện phép tính hóa đơn đầu tiên.

## Phạm vi

InvoiceLib hiện là thư viện core dạng JAR. Thư viện chỉ tính toán dữ liệu hóa đơn trong bộ nhớ:

- Không mở HTTP endpoint.
- Không tự kết nối database.
- Không cần chạy một process riêng.
- Không phụ thuộc Spring Boot.

Ứng dụng khách hàng chịu trách nhiệm lưu hóa đơn, phân quyền, xuất chứng từ và tích hợp hệ thống bên ngoài.

## Yêu cầu

- Java 21 trở lên.
- Maven 3.9 trở lên nếu build hoặc cài từ source.

## Build và cài đặt local

Tại thư mục gốc InvoiceLib:

```bash
mvn clean install
```

Lệnh này biên dịch source, chạy test và cài `invoicelib-0.0.1.jar` vào Maven local repository.

Thêm dependency vào `pom.xml` của ứng dụng khách hàng:

```xml
<dependency>
	<groupId>com.huydungktv</groupId>
	<artifactId>invoicelib</artifactId>
	<version>0.0.1</version>
</dependency>
```

Nếu dùng repository nội bộ, thay dependency trên bằng version đã được publish tại repository đó.

## Tính hóa đơn

```java
import com.huydungktv.invoice.api.InvoiceItemRequest;
import com.huydungktv.invoice.api.InvoiceResponse;
import com.huydungktv.invoice.service.DefaultInvoiceCalculator;
import com.huydungktv.invoice.service.InvoiceCalculator;

import java.math.BigDecimal;
import java.util.List;

InvoiceCalculator calculator = new DefaultInvoiceCalculator();

InvoiceResponse result = calculator.calculate(List.of(
		new InvoiceItemRequest(
				"Laptop",
				new BigDecimal("2"),
				new BigDecimal("1000.00"),
				new BigDecimal("10")
		)
));

BigDecimal totalBeforeVat = result.total();
BigDecimal totalVat = result.totalPriceVat();
BigDecimal totalAfterVat = result.totalVat();
```

Với dữ liệu trên, kết quả là:

```text
totalBeforeVat = 2000.00
totalVat       = 200.00
totalAfterVat  = 2200.00
```

## Lưu ý khi tạo BigDecimal

Luôn tạo số tiền từ chuỗi hoặc số nguyên:

```java
new BigDecimal("1000.50")
```

Không dùng số thực `double` cho tiền tệ vì có thể tạo sai số nhị phân:

```java
new BigDecimal(1000.50) // Không khuyến nghị
```

## Kiểm tra kết quả

`result.items()` chứa kết quả của từng dòng. Các tổng cấp hóa đơn nằm trên `result`:

```java
result.items().forEach(item -> {
	System.out.println(item.itemName());
	System.out.println(item.subTotal());
	System.out.println(item.priceVat());
	System.out.println(item.subTotalVat());
});
```

## Xuất hóa đơn thành CSV

Sau khi có `InvoiceResponse`, dùng `InvoiceCsvMapper` để tạo nội dung CSV:

```java
import com.huydungktv.invoice.mapper.InvoiceCsvMapper;

InvoiceCsvMapper csvMapper = new InvoiceCsvMapper();
String csvContent = csvMapper.toCsv(result);
```

Thư viện trả về nội dung dạng `String` và không tự ghi file. Ứng dụng khách hàng quyết định tên file, encoding và nơi lưu:

```java
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

Files.writeString(
	Path.of("invoice.csv"),
	csvContent,
	StandardCharsets.UTF_8
);
```

CSV có một dòng header, các dòng `ITEM` và một dòng `TOTAL`:

```text
rowType,itemName,quantity,price,vat,priceVat,subTotal,subTotalVat,total,totalPriceVat,totalVat
ITEM,Laptop,2,1000.00,10,200.00,2000.00,2200.00,,,
TOTAL,,,,,,,,2000.00,200.00,2200.00
```

`InvoiceCsvMapper` tự escape item name chứa dấu phẩy, dấu nháy kép hoặc ký tự xuống dòng theo quy tắc CSV.

Khi chạy ở mức log mặc định, mapper ghi ra console số lượng item khi bắt đầu và các tổng hóa đơn khi hoàn tất.

## Xử lý input không hợp lệ

Input không hợp lệ sẽ ném `InvoiceValidationException`. Ứng dụng khách hàng nên bắt exception tại lớp biên, ghi log phù hợp và trả thông báo lỗi theo contract của chính ứng dụng đó.

## Build artifact

Để chỉ tạo JAR:

```bash
mvn clean package
```

Artifact nằm tại:

```text
target/invoicelib-0.0.1.jar
```
