# Changelog

## 0.0.1

- Thêm `InvoiceCsvMapper.toCsv(InvoiceResponse)` để chuyển output hóa đơn thành nội dung CSV.
- CSV hỗ trợ dòng `ITEM`, dòng `TOTAL` và escape giá trị chứa dấu phẩy, dấu nháy kép hoặc xuống dòng.
- Thêm logging mức `INFO` cho quá trình bắt đầu và hoàn tất chuyển đổi CSV.
- Thêm `InvoicePdfMapper.toPdf(InvoiceResponse)` để tạo PDF và sinh số hóa đơn theo `yyyyMMdd.HHmmss`.
- Thêm `InvoicePdfResult` chứa số hóa đơn và `pdfBytes`.
- Cập nhật tài liệu hướng dẫn sử dụng và tích hợp CSV.
