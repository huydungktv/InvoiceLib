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
