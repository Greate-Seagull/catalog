### Nhóm CAT: Danh mục hàng hóa và dịch vụ

| Mã | Yêu cầu | Ưu tiên | GĐ |
|---|---|---|---|
| FR-CAT-001 | Tạo, sửa, ẩn, xóa mềm sản phẩm với tên, mã SKU, mã vạch, danh mục, ảnh, giá bán, giá vốn, thuế suất | M | R1 |
| FR-CAT-002 | Sản phẩm có biến thể (size, màu) với SKU và tồn kho riêng | S | R2 |
| FR-CAT-003 | Đa đơn vị tính và quy đổi (nhập thùng, bán lẻ cái/gói) | M | R1 |
| FR-CAT-004 | Quản lý theo lô và hạn sử dụng (lô/date), tự động xuất theo FEFO/FIFO | S | R2 |
| FR-CAT-005 | Combo và bộ sản phẩm; sản phẩm dịch vụ không theo dõi tồn | S | R2 |
| FR-CAT-006 | Nhập và xuất danh mục hàng loạt qua Excel/CSV, có báo cáo lỗi từng dòng | M | R1 |
| FR-CAT-007 | Nhiều bảng giá (giá lẻ, giá sỉ, giá theo cửa hàng, giá theo kênh) | S | R2 |
| FR-CAT-008 | Chương trình khuyến mãi: giảm theo phần trăm/số tiền, mua X tặng Y, theo khung giờ, theo nhóm khách | S | R2 |
| FR-CAT-009 | Mỗi mặt hàng gắn ngành nghề/mã ngành để xác định thuế suất áp dụng khi tính thuế | M | R1 |
| FR-CAT-010 | Sản phẩm có định lượng nguyên liệu (công thức) cho F&B | C | R3 |
| FR-CAT-011 | Quét mã vạch để tìm hoặc thêm sản phẩm; hỗ trợ tự sinh mã nội bộ | M | R1 |
| FR-CAT-012 | Lịch sử thay đổi giá và thông tin sản phẩm | S | R2 |
| NFR-PERF-003 | Tìm sản phẩm theo tên/mã vạch | p95 ≤ 200 ms với danh mục tới 50.000 SKU/tenant | M |
| NFR-USAB-003 | Ngôn ngữ | Giao diện tiếng Việt đầy đủ, thuật ngữ thuế chính xác và có giải thích; định dạng tiền tệ VND, ngày tháng theo Việt Nam | M |
| NFR-USAB-004 | Ngôn ngữ khác | Thiết kế cho phép thêm ngôn ngữ (tiếng Anh) mà không đổi mã (tách chuỗi văn bản) | C |
| NFR-USAB-005 | Khả năng tiếp cận | Chữ đủ lớn, độ tương phản đạt WCAG 2.1 AA cho các màn hình chính; thao tác chạm đủ lớn cho màn hình nhỏ | S |
| NFR-MAINT-008 | Quy ước mã | Chuẩn định dạng, phân tích tĩnh, đánh giá mã bắt buộc | S |